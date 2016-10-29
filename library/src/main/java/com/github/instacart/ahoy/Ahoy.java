package com.github.instacart.ahoy;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.github.instacart.ahoy.utils.ActivityCounter;
import com.github.instacart.ahoy.utils.ActivityCounter.FirstActivityStartedListener;
import com.github.instacart.ahoy.utils.ActivityCounter.LastActivityStoppedListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class Ahoy {

    private static final String TAG = "ahoy";

    private ActivityCounter activityCounter = new ActivityCounter();
    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    private final AhoyDelegate delegate;
    private final Storage storage;
    private Visit visit;
    private volatile boolean visitResetLock;
    private List<VisitListener> visitListeners = new ArrayList<>();
    private String visitorToken;

    public interface VisitListener {
        void onVisitUpdated(Visit visit);
    }

    public Ahoy(Application application, final AhoyDelegate delegate) {
        this.delegate = delegate;
        storage = new Storage(application);
        visit = storage.getVisit();
        visitorToken = storage.getVisitorToken(null);
        if (TextUtils.isEmpty(visitorToken)) {
            visitorToken = delegate.newVisitorToken();
        }
        activityCounter.addFistActivityStartedListener(new FirstActivityStartedListener() {
            @Override public void onFirstActivityStarted(Activity activity) {
                visitResetLock = false;
                scheduleReset(visit != null ? visit.expiresAt() : System.currentTimeMillis());
            }
        });
        activityCounter.addLastActivityStoppedListener(new LastActivityStoppedListener() {
            @Override public void onLastActivityStopped(Activity activity) {
                compositeSubscription.clear();
            }
        });
        application.registerActivityLifecycleCallbacks(activityCounter);
    }

    private void scheduleReset(long timestamp) {
        long delay = Math.max(timestamp - System.currentTimeMillis(), 0);
        compositeSubscription.add(
                Observable.timer(delay, TimeUnit.MILLISECONDS)
                        .subscribe(new Action1<Long>() {
                            @Override public void call(Long aLong) {
                                Log.d(TAG, "reseting visit ");
                                reset(Collections.<String, Object>emptyMap());
                            }
                        }));
    }

    private void reset(final Map<String, Object> extraParameters) {
        if (visitResetLock) {
            return;
        }

        visitResetLock = true;

        VisitCallbackOnSubscribe visitCallbackOnSubscribe = new VisitCallbackOnSubscribe();
        compositeSubscription.add(
                Observable.create(visitCallbackOnSubscribe)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Visit>() {
                    @Override public void call(Visit visit) {
                        Ahoy.this.visit = visit;
                        Log.d(TAG, "new visit " + visit.visitToken());
                        storage.setVisit(visit);
                        visitResetLock = false;
                        fireVisitUpdatedEvent();
                        scheduleReset(visit.expiresAt());
                    }
                }, new Action1<Throwable>() {
                    @Override public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        Log.d(TAG, "failed registering a visit " + visit.visitToken());
                        visitResetLock = false;
                    }
                }));

        VisitParams params = VisitParams.create(visitorToken, null, extraParameters);
        delegate.newVisit(params, visitCallbackOnSubscribe);
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

    @Nullable public Visit getVisit() {
        return visit;
    }

    public String getVisitorToken() {
        return visitorToken;
    }

    public void addVisitListener(VisitListener listener) {
        visitListeners.add(listener);
    }

    public void removeVisitListener(VisitListener listener) {
        visitListeners.remove(listener);
    }

    public void resetVisit(Map<String, Object> extraParameters) {
        reset(extraParameters);
    }
}