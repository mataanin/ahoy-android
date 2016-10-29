package com.github.instacart.ahoy.utils;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

public class ActivityCounter extends ActivityLifecycleCallbacksStub {

    private int mVisibleActivitiesCounter = 0;
    private List<FirstActivityStartedListener> firstActivityStartedListeners = new ArrayList<>();
    private List<LastActivityStoppedListener> lastActivityStoppedListeners = new ArrayList<>();

    public interface FirstActivityStartedListener {
        void onFirstActivityStarted(Activity activity);
    }

    public interface LastActivityStoppedListener {
        void onLastActivityStopped(Activity activity);
    }

    @Override public void onActivityStarted(Activity activity) {
        if (mVisibleActivitiesCounter == 0) {
            fireFirstActivityStarted(activity);
        }

        mVisibleActivitiesCounter++;
    }

    @Override public void onActivityStopped(Activity activity) {
        mVisibleActivitiesCounter--;

        if (mVisibleActivitiesCounter == 0) {
            fireLastActivityStopped(activity);
        }
    }

    private void fireFirstActivityStarted(Activity activity) {
        for (FirstActivityStartedListener listener : firstActivityStartedListeners) {
            listener.onFirstActivityStarted(activity);
        }
    }

    private void fireLastActivityStopped(Activity activity) {
        for (LastActivityStoppedListener listener : lastActivityStoppedListeners) {
            listener.onLastActivityStopped(activity);
        }
    }

    public void addFistActivityStartedListener(FirstActivityStartedListener listener) {
        firstActivityStartedListeners.add(listener);
    }

    public void addLastActivityStoppedListener(LastActivityStoppedListener listener) {
        lastActivityStoppedListeners.add(listener);
    }
}