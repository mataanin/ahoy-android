package com.github.instacart.ahoy;

import android.app.Application;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.instacart.ahoy.utils.SharedPreferencesWrapper;
import com.github.instacart.ahoy.utils.TypeUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class Storage {

    private static final String AHOY_PREFERENCES_FILE = "ahoy.prefs";

    private static final String VISIT_TOKEN = "visit token";
    private static final String VISIT_EXTRA_PARAMS = "visit extra params";
    private static final String VISITOR_TOKEN = "visitor token";
    private static final String VISIT_EXPIRATION = "visit expiration";
    private static final String PENDING_EXTRA_PARAMS = "pending extra params";

    private final SharedPreferencesWrapper sharedPreferences;

    public Storage(Application application) {
        sharedPreferences = new SharedPreferencesWrapper(application, AHOY_PREFERENCES_FILE);
    }

    @Nullable public Visit readVisit() {
        String visitToken = sharedPreferences.getString(VISIT_TOKEN, null);
        if (visitToken == null) {
            return null;
        }
        Map<String, String> emptyMap = Collections.emptyMap();
        Map<String, String> extraParams = sharedPreferences.getStringMap(VISIT_EXTRA_PARAMS, "", emptyMap);
        long visitTokenExpiration = sharedPreferences.getLong(VISIT_EXPIRATION, 0);
        return Visit.create(visitToken, extraParams, visitTokenExpiration);
    }

    public void saveVisit(Visit visit) {
        if (visit == null) {
            sharedPreferences.delete(VISIT_EXPIRATION);
            sharedPreferences.delete(VISIT_TOKEN);
            return;
        }

        sharedPreferences.putLong(VISIT_EXPIRATION, visit.expiresAt());
        sharedPreferences.putString(VISIT_TOKEN, visit.visitToken());
    }

    public void saveVisitorToken(String visitorToken) {
        sharedPreferences.putString(VISITOR_TOKEN, visitorToken);
    }

    public String readVisitorToken(String defaultValue) {
        return sharedPreferences.getString(VISITOR_TOKEN, defaultValue);
    }

    public void updatePendingExtraParams(@Nullable Map<String, String> extraParams) {
        if (extraParams == null) {
            sharedPreferences.putStringMap(PENDING_EXTRA_PARAMS, null);
        } else {
            Map<String, String> params = TypeUtil.ifNull(readPendingExtraParams(null), new HashMap<String, String>());
            params.putAll(extraParams);
            sharedPreferences.putStringMap(PENDING_EXTRA_PARAMS, params);
        }
        Log.d("foobar", "updatePendingExtraParams " + (extraParams != null ? extraParams.toString() : "null"));
    }

    public Map<String, String> readPendingExtraParams(Map<String, String> defaultValue) {
        Map<String, String> map = sharedPreferences.getStringMap(PENDING_EXTRA_PARAMS, "", defaultValue);
        Log.d("foobar", "readPendingExtraParams " + (map != null ? map.toString() : "null"));
        return map;
    }
}
