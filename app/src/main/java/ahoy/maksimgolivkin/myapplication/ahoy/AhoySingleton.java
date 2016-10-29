package ahoy.maksimgolivkin.myapplication.ahoy;

import android.app.Application;
import android.support.annotation.Nullable;

import java.util.Map;

import ahoy.maksimgolivkin.myapplication.ahoy.Ahoy.VisitListener;

public class AhoySingleton {

    private static Ahoy sInstance;

    public static void init(Application application, AhoyDelegate delegate) {
        sInstance = new Ahoy(application, delegate);
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

    public static void resetVisit(Map<String, Object> extraParams) {
        sInstance.resetVisit(extraParams);
    }
}
