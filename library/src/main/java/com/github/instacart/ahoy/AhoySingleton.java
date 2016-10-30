package com.github.instacart.ahoy;

import android.app.Application;
import android.support.annotation.Nullable;

import com.github.instacart.ahoy.Ahoy.VisitListener;
import com.github.instacart.ahoy.delegate.AhoyDelegate;

import java.util.Map;

import rx.Observable;

public class AhoySingleton {

    private static Ahoy sInstance;

    public static void init(Application application, AhoyDelegate delegate, boolean autoStart) {
        sInstance = new Ahoy(application, delegate, autoStart);
    }

    @Nullable public static Visit getVisit() {
        return sInstance.getVisit();
    }

    public static String getVisitorToken() {
        return sInstance.getVisitorToken();
    }

    public static void addVisitListener(VisitListener listener) {
        sInstance.addVisitListener(listener);
    }

    public static void removeVisitListener(VisitListener listener) {
        sInstance.removeVisitListener(listener);
    }

    public static Observable<Visit> visitStream() {
        return RxAhoy.stream(sInstance);
    }

    public static void scheduleNewVisit(Map<String, String> extraParams) {
        sInstance.scheduleNewVisit(extraParams);
    }

    public static void scheduleSaveExtras(Map<String, String> extraParams) {
        sInstance.sheduleSaveExtras(extraParams);
    }
}