package com.github.instacart.ahoy;

import android.app.Activity;

import com.github.instacart.ahoy.utils.ActivityLifecycleCallbacksStub;

abstract class LifecycleCallbacks extends ActivityLifecycleCallbacksStub {

    private int visibleActivitiesCounter = 0;

    @Override public void onActivityStarted(Activity activity) {
        visibleActivitiesCounter++;
    }

    @Override public void onActivityStopped(Activity activity) {
        visibleActivitiesCounter--;

        if (visibleActivitiesCounter == 0) {
            onLastOnStop();
        }
    }

    protected abstract void onLastOnStop();
}