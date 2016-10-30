package com.github.instacart.ahoy;

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

import java.util.Collections;
import java.util.Map;

@AutoValue
public abstract class Visit {

    public static final String OS = "os";
    public static final String REFERRER = "referrer";
    public static final String STARTED_AT = "started_at";
    public static final String UTM_CAMPAIGN = "utm_campaign";
    public static final String UTM_CONTENT = "utm_content";
    public static final String UTM_MEDIUM = "utm_medium";
    public static final String UTM_SOURCE = "utm_source";
    public static final String UTM_TERM = "utm_term";
    public static final String VISIT_TOKEN = "visit_token";
    public static final String VISITOR_TOKEN = "visitor_token";

    public static final String OS_ANDROID = "android";

    public static Visit create(String visitToken, @NonNull Map<String, String> extraParams, long expiresAt) {
        return new AutoValue_Visit(visitToken, Collections.unmodifiableMap(extraParams), expiresAt);
    }

    public abstract String visitToken();
    public abstract Map<String, String> extraParams();
    public abstract long expiresAt();

    public boolean isValid() {
        return System.currentTimeMillis() < expiresAt();
    }

    public Visit expire() {
        return Visit.create(visitToken(), extraParams(), System.currentTimeMillis());
    }
}
