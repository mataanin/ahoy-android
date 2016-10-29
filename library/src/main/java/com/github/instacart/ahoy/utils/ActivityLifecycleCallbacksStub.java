package com.github.instacart.ahoy.utils;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;

public class ActivityLifecycleCallbacksStub implements ActivityLifecycleCallbacks {

    @Override public void onActivityCreated(Activity activity, Bundle bundle) {
    }

    @Override public void onActivityStarted(Activity activity) {
    }

    @Override public void onActivityResumed(Activity activity) {
    }

    @Override public void onActivityPaused(Activity activity) {
    }

    @Override public void onActivityStopped(Activity activity) {
    }

    @Override public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override public void onActivityDestroyed(Activity activity) {
    }
}
