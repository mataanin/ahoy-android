package com.github.instacart.ahoy;

import android.support.annotation.NonNull;

import com.github.instacart.ahoy.utils.TypeUtil;
import com.google.auto.value.AutoValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@AutoValue
public abstract class Visit {

    public static final String APP_VERSION = "app_version";
    public static final String LANDING_PAGE = "landing_page";
    public static final String OS = "os";
    public static final String OS_ANDROID = "android";
    public static final String OS_VERSION = "os_version";
    public static final String REFERRER = "referrer";
    public static final String STARTED_AT = "started_at";
    public static final String VISIT_TOKEN = "visit_token";
    public static final String VISITOR_TOKEN = "visitor_token";
    public static final String UTM_CAMPAIGN = "utm_campaign";
    public static final String UTM_CONTENT = "utm_content";
    public static final String UTM_MEDIUM = "utm_medium";
    public static final String UTM_SOURCE = "utm_source";
    public static final String UTM_TERM = "utm_term";
    public static final String PLATFORM = "platform";

    public static Visit create(String visitToken, @NonNull Map<String, Object> extraParams, long expiresAt) {
        extraParams = TypeUtil.ifNull(extraParams, Collections.<String, Object>emptyMap());
        return new AutoValue_Visit(visitToken, Collections.unmodifiableMap(extraParams), expiresAt);
    }

    public abstract String visitToken();
    public abstract Map<String, Object> extraParams();
    public abstract long expiresAt();

    public boolean isValid() {
        return System.currentTimeMillis() < expiresAt();
    }

    public Visit expire() {
        return Visit.create(visitToken(), extraParams(), System.currentTimeMillis());
    }

    public Visit withUpdatedExtraParams(Map<String, Object> extraParams) {
        Map<String, Object> map = new HashMap<String, Object>(extraParams());
        map.putAll(extraParams);
        return Visit.create(visitToken(), map, expiresAt());
    }

    public static Visit empty() {
        return create("", Collections.<String, Object>emptyMap(), 0);
    }
}
