package ahoy.maksimgolivkin.myapplication.ahoy;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ahoy.maksimgolivkin.myapplication.ahoy.utils.ActivityCounter;
import ahoy.maksimgolivkin.myapplication.ahoy.utils.ActivityCounter.FirstActivityStartedListener;
import ahoy.maksimgolivkin.myapplication.ahoy.utils.ActivityCounter.LastActivityStoppedListener;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class Ahoy {

    private static final String TAG = "ahoy-manager";

    private ActivityCounter activityCounter = new ActivityCounter();
    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    private final AhoyDelegate delegate;
    private final Storage storage;
    private Visit visit;
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
        compositeSubscription.add(delegate.newVisit(visitorToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Visit>() {
                    @Override public void call(Visit visit) {
                        Ahoy.this.visit = visit;
                        Log.d(TAG, "received visit " + visit.visitToken());
                        storage.setVisit(visit);
                        delegate.registerVisit(visitorToken, visit, extraParameters);
                        scheduleReset(visit.expiresAt());
                        fireVisitUpdatedEvent();
                    }
                }));
    }

    private void fireVisitUpdatedEvent() {
        for (VisitListener listener : visitListeners) {
            listener.onVisitUpdated(visit);
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