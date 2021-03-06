package com.github.instacart.ahoy;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.github.instacart.ahoy.delegate.AhoyDelegate;
import com.github.instacart.ahoy.delegate.VisitParams;
import com.google.auto.value.AutoValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.github.instacart.ahoy.Ahoy.Request.Type.NEW_VISIT;
import static com.github.instacart.ahoy.Ahoy.Request.Type.UPDATE;

public class Ahoy {

    private static final String TAG = "ahoy";
    private static final long RETRY_DELAY = 1000;

    private CompositeSubscription scheduledSubscriptions = new CompositeSubscription();
    private CompositeSubscription updatesSubscription = new CompositeSubscription();

    private final AhoyDelegate delegate;
    private final Scheduler singleThreadScheduler = Schedulers.from(Executors.newSingleThreadExecutor());
    private final Storage storage;
    private Visit visit;
    private List<VisitListener> visitListeners = new ArrayList<>();
    private String visitorToken;

    private ArrayList<Request> updateQueue = new ArrayList<>();

    private volatile boolean updateLock = false;
    private boolean shutdown;

    public interface VisitListener {
        void onVisitUpdated(Visit visit);
    }

    @AutoValue
    public static abstract class Request {

        enum Type {
            NEW_VISIT, UPDATE
        }

        public static Request newVisit(VisitParams visitParams) {
            return new AutoValue_Ahoy_Request(NEW_VISIT, visitParams);
        }

        public static Request update(VisitParams visitParams) {
            return new AutoValue_Ahoy_Request(UPDATE, visitParams);
        }

        public abstract Type getType();
        public abstract VisitParams getVisitParams();

        public boolean isNewVisit() {
            return NEW_VISIT.equals(getType());
        }
    }

    public Ahoy(Application application, final AhoyDelegate delegate, final boolean autoStart) {
        this.delegate = delegate;

        storage = new Storage(application);
        visit = storage.readVisit(Visit.empty());
        visitorToken = storage.readVisitorToken(null);

        if (TextUtils.isEmpty(visitorToken)) {
            visitorToken = delegate.newVisitorToken();
            storage.saveVisitorToken(visitorToken);
        }

        application.registerActivityLifecycleCallbacks(new LifecycleCallbacks() {

            @Override public void onActivityCreated(Activity activity, Bundle bundle) {
                super.onActivityCreated(activity, bundle);
                if (!autoStart || shutdown) {
                    return;
                }
                scheduleUpdate(System.currentTimeMillis());
            }

            @Override public void onActivityStarted(Activity activity) {
                super.onActivityStarted(activity);
                if (!autoStart || shutdown) {
                    return;
                }
                scheduleUpdate(System.currentTimeMillis());
            }

            @Override public void onLastOnStop() {
                updatesSubscription.clear();
                scheduledSubscriptions.clear();
                updateLock = false;
            }
        });
    }

    private void scheduleUpdate(long timestamp) {
        scheduledSubscriptions.clear();
        final long delay = Math.max(timestamp - System.currentTimeMillis(), 0);
        Timber.tag(TAG).d(String.format("schedule update with delay %d at %d", delay, System.currentTimeMillis()));
        scheduledSubscriptions.add(
                Observable.timer(delay, TimeUnit.MILLISECONDS)
                        .observeOn(singleThreadScheduler)
                        .subscribe(new Action1<Long>() {
                            @Override public void call(Long aLong) {
                                Timber.tag(TAG).d(String.format("update at %d", System.currentTimeMillis()));
                                if (!visit.isValid()) {
                                    enqueueExpiredVisitUpdate();
                                }
                                processQueue();
                            }
                        }));
    }

    private void enqueueExpiredVisitUpdate() {
        synchronized (updateQueue) {
            for (Request request : updateQueue) {
                if (request.isNewVisit()) {
                    return;
                }
            }
            updateQueue.add(Request.newVisit(VisitParams.create(visitorToken, null, null)));
        }
    }

    private void processQueue() {
        if (updateLock) {
            return;
        }
        updateLock = true;

        synchronized (updateQueue) {

            if (updateQueue.size() == 0) {
                updateLock = false;
                scheduleUpdate(visit.expiresAt());
                return;
            }

            final Request request = updateQueue.get(0);

            VisitCallbackOnSubscribe visitCallbackOnSubscribe = new VisitCallbackOnSubscribe();
            updatesSubscription.add(
                    Observable.create(visitCallbackOnSubscribe)
                            .subscribe(new Action1<Visit>() {
                                @Override public void call(Visit visit) {
                                    saveVisit(visit);
                                    synchronized (updateQueue) {
                                        updateQueue.remove(0);
                                    }
                                    updateLock = false;
                                    scheduleUpdate(0);
                                }
                            }, new Action1<Throwable>() {
                                @Override public void call(Throwable throwable) {
                                    throwable.printStackTrace();
                                    updateLock = false;
                                    Timber.tag(TAG).d("failed " + request.getType() + " " + request.getVisitParams());
                                    scheduleUpdate(System.currentTimeMillis() + RETRY_DELAY);
                                }
                            }));

            if (request.isNewVisit()) {
                delegate.saveVisit(request.getVisitParams(), visitCallbackOnSubscribe);
            } else {
                delegate.saveExtras(request.getVisitParams(), visitCallbackOnSubscribe);
            }
        }
    }


    private void saveVisit(Visit visit) {
        if (visit == null) {
            throw new IllegalArgumentException("visit can't be null");
        }

        Visit oldVisit = this.visit;
        this.visit = visit;
        Timber.tag(TAG).d("saving updated visit " + visit.toString());
        storage.saveVisit(visit);
        if (!oldVisit.equals(visit)) {
            fireVisitUpdatedEvent();
        }
    }

    private void fireVisitUpdatedEvent() {
        for (VisitListener listener : visitListeners) {
            try {
                listener.onVisitUpdated(visit);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @NonNull public Visit visit() {
        return visit;
    }

    public String visitorToken() {
        return visitorToken;
    }

    public void addVisitListener(VisitListener listener) {
        visitListeners.add(listener);
    }

    public void removeVisitListener(VisitListener listener) {
        visitListeners.remove(listener);
    }

    /**
     * Save visit with the provided extra parameters. If a visit was already saved, new visit is started
     * <p>
     * New values for the same keys, override stored parameters.
     * <p>
     * the {@link AhoyDelegate}.
     *
     * @param extraParams Extra parameters passed to {@link AhoyDelegate}. Null will saved parameters.
     */
    public void newVisit(@Nullable Map<String, Object> extraParams) {
        if (shutdown) {
            throw new IllegalArgumentException("Ahoy has been shutdownAndClear");
        }
        visit = visit.expire();
        synchronized (updateQueue) {
            updateQueue.add(Request.newVisit(VisitParams.create(visitorToken, null, extraParams)));
        }
        scheduleUpdate(System.currentTimeMillis());
    }

    /**
     * Save visit with the provided extra parameters. If a visit was already saved, new visit is started
     * <p>
     * New values for the same keys, override stored parameters.
     * <p>
     * the {@link AhoyDelegate}.
     *
     * @param extraParams Extra parameters passed to {@link AhoyDelegate}. Null will saved parameters.
     */
    public void ensureFreshVisit(@Nullable Map<String, Object> extraParams) {
        if (shutdown) {
            throw new IllegalArgumentException("Ahoy has been shutdownAndClear");
        }
        scheduleUpdate(System.currentTimeMillis());
    }

    /**
     * Save extra visit parameters as part of save visit or save extras delegate call.
     * <p>
     * New values for the same keys, override stored parameters.
     * <p>
     * the {@link AhoyDelegate}.
     *
     * @param extraParams Extra parameters passed to {@link AhoyDelegate}. Null will saved parameters.
     */
    public void saveExtras(@Nullable Map<String, Object> extraParams) {
        if (shutdown) {
            throw new IllegalArgumentException("Ahoy has been shutdownAndClear");
        }

        synchronized (updateQueue) {
            updateQueue.add(Request.update(VisitParams.create(visitorToken, visit, extraParams)));
        }
        scheduleUpdate(System.currentTimeMillis());
    }

    public void shutdownAndClear() {
        storage.clear();
        updatesSubscription.clear();
        scheduledSubscriptions.clear();
        shutdown = true;
    }

    public Visit getVisit() {
        return visit;
    }

    public String getVisitorToken() {
        return visitorToken;
    }
}