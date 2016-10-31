package com.github.instacart.ahoy;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.github.instacart.ahoy.delegate.AhoyDelegate;
import com.github.instacart.ahoy.delegate.VisitParams;
import com.github.instacart.ahoy.utils.TypeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class Ahoy {

    private static final String TAG = "ahoy";

    private static final long RETRY_DELAY = 2000;

    private CompositeSubscription scheduledSubscriptions = new CompositeSubscription();
    private CompositeSubscription updatesSubscription = new CompositeSubscription();

    private final AhoyDelegate delegate;
    private final Scheduler singleThreadScheduler = Schedulers.from(Executors.newSingleThreadExecutor());
    private final Storage storage;
    private Visit visit;
    private List<VisitListener> visitListeners = new ArrayList<>();
    private String visitorToken;

    private volatile boolean updateLock = false;

    public interface VisitListener {
        void onVisitUpdated(Visit visit);
    }

    private class UpdateAction implements Action0 {

        private boolean expireVisit;
        private Map<String, Object> newExtraParams;

        public UpdateAction() {
        }

        public UpdateAction(boolean epxireVisit, Map<String, Object> newVisitExtraParams) {
            this.expireVisit = epxireVisit;
            this.newExtraParams = newVisitExtraParams;
        }

        @Override public void call() {

            Log.d(TAG, "running scheduled update");
            if (updateLock) {
                scheduleUpdate(System.currentTimeMillis() + RETRY_DELAY);
                return;
            }

            updateLock = true;

            if (expireVisit) {
                saveVisit(visit.expire());
                storage.updatePendingExtraParams(newExtraParams);
            }
            Map<String, Object> emptyMap = Collections.emptyMap();
            Map<String, Object> extraParameters = storage.readPendingExtraParams(emptyMap);
            if (!visit.isValid()) {
                newVisit(extraParameters);
            } else if (!TypeUtil.isEmpty(extraParameters)){
                saveExtraParams(extraParameters);
            } else {
                updateLock = false;
            }

            if (!updateLock && visit.isValid()) {
                scheduleUpdate(visit.expiresAt());
            }
        }
    }

    private class FailureAction implements Action1<Throwable> {

        private final String operation;
        private final VisitParams visitParams;

        public FailureAction(String operation, VisitParams visitParams) {
            this.operation = operation;
            this.visitParams = visitParams;
        }

        @Override public void call(Throwable throwable) {
            throwable.printStackTrace();
            updateLock = false;
            Log.d(TAG, "failed " + operation + " " + visitParams);
            scheduleUpdate(System.currentTimeMillis() + RETRY_DELAY);
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
                if (!autoStart) {
                    return;
                }
                scheduleUpdate(visit.expiresAt());
            }

            @Override public void onLastOnStop() {
                shutdown();
            }
        });
    }

    private void scheduleUpdate(long timestamp) {
        scheduleUpdate(timestamp, new UpdateAction());
    }

    private void scheduleUpdate(long timestamp, final Action0 update) {
        scheduledSubscriptions.clear();
        final long delay = Math.max(timestamp - System.currentTimeMillis(), 0);
        Log.d(TAG, String.format("schedule update with delay %d at %d", delay, System.currentTimeMillis()));
        scheduledSubscriptions.add(
                Observable.timer(delay, TimeUnit.MILLISECONDS)
                        .observeOn(singleThreadScheduler)
                        .subscribe(new Action1<Long>() {
                            @Override public void call(Long aLong) {
                                Log.d(TAG, String.format("called at %d", System.currentTimeMillis()));
                                update.call();
                            }
                        }));
    }

    private void newVisit(final Map<String, Object> extraParameters) {
        VisitCallbackOnSubscribe visitCallbackOnSubscribe = new VisitCallbackOnSubscribe();
        final VisitParams visitParams = VisitParams.create(visitorToken, null, extraParameters);
        updatesSubscription.add(
                Observable.create(visitCallbackOnSubscribe)
                        .subscribe(new Action1<Visit>() {
                            @Override public void call(Visit visit) {
                                saveVisit(visit);
                                storage.updatePendingExtraParams(null);
                                updateLock = false;
                            }
                        }, new FailureAction("on new visit", visitParams)));
        delegate.saveVisit(visitParams, visitCallbackOnSubscribe);
    }

    private void saveExtraParams(final Map<String, Object> extraParameters) {
        VisitCallbackOnSubscribe visitCallbackOnSubscribe = new VisitCallbackOnSubscribe();
        final VisitParams visitParams = VisitParams.create(visitorToken, visit, extraParameters);
        updatesSubscription.add(
                Observable.create(visitCallbackOnSubscribe)
                        .subscribe(new Action1<Visit>() {
                            @Override public void call(Visit visit) {
                                saveVisit(visit.withUpdatedExtraParams(extraParameters));
                                storage.updatePendingExtraParams(null);
                                updateLock = false;
                            }
                        }, new FailureAction("saving extra parameters", visitParams)));
        delegate.saveExtras(visitParams, visitCallbackOnSubscribe);
    }

    private void saveVisit(Visit visit) {
        if (visit == null) {
            throw new IllegalArgumentException("visit can't be null");
        }

        Visit oldVisit = this.visit;
        this.visit = visit;
        Log.d(TAG, "saving updated visit " + visit.toString());
        storage.saveVisit(visit);
        if (!oldVisit.equals(visit)) {
            fireVisitUpdatedEvent();
        }
        scheduleUpdate(visit.expiresAt());
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

    public void scheduleNewVisit(@Nullable Map<String, Object> extraParams) {
        visit = visit.expire();
        storage.updatePendingExtraParams(extraParams);
        scheduleUpdate(System.currentTimeMillis(), new UpdateAction(true, extraParams));
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
    public void scheduleSaveExtras(@Nullable Map<String, Object> extraParams) {
        storage.updatePendingExtraParams(extraParams);
        scheduleUpdate(System.currentTimeMillis());
    }

    public void shutdown() {
        updatesSubscription.clear();
        scheduledSubscriptions.clear();
        updateLock = false;
    }
}