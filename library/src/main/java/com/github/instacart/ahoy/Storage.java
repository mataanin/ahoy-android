package com.github.instacart.ahoy;

import android.app.Application;
import android.support.annotation.Nullable;

import com.github.instacart.ahoy.utils.SharedPreferencesWrapper;

public class Storage {

    private static final String AHOY_PREFERENCES_FILE = "com.com.github.instacart.prefs";

    private static final String VISIT_TOKEN = "visit token";
    private static final String VISITOR_TOKEN = "visitor token";
    private static final String VISIT_EXPIRATION = "visit expiration";

    private final SharedPreferencesWrapper sharedPreferences;

    public Storage(Application application) {
        sharedPreferences = new SharedPreferencesWrapper(application, AHOY_PREFERENCES_FILE);
    }

    @Nullable public Visit getVisit() {
        String visitToken = sharedPreferences.getString(VISIT_TOKEN, null);
        if (visitToken == null) {
            return null;
        }

        long visitTokenExpiration = sharedPreferences.getLong(VISIT_EXPIRATION, 0);
        return Visit.create(visitToken, visitTokenExpiration);
    }

    public void setVisit(Visit visit) {
        sharedPreferences.putLong(VISIT_EXPIRATION, visit.expiresAt());
        sharedPreferences.putString(VISIT_TOKEN, visit.visitToken());
    }

    public void setVisitorToken(String visitorToken) {
        sharedPreferences.putString(VISITOR_TOKEN, visitorToken);
    }

    public String getVisitorToken(String defaultValue) {
        return sharedPreferences.getString(VISITOR_TOKEN, defaultValue);
    }
}
