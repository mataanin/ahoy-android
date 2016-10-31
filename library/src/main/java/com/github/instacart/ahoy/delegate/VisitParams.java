package com.github.instacart.ahoy.delegate;

import android.support.annotation.Nullable;

import com.github.instacart.ahoy.Visit;
import com.google.auto.value.AutoValue;

import java.util.HashMap;
import java.util.Map;

@AutoValue
public abstract class VisitParams {

    private static final String[] utmParams = {
            Visit.UTM_CAMPAIGN,
            Visit.UTM_CONTENT,
            Visit.UTM_MEDIUM,
            Visit.UTM_SOURCE,
            Visit.UTM_TERM
    };

    public static VisitParams create(String visitorToken, @Nullable Visit visit,
            @Nullable Map<String, Object> extraParams) {
        return new AutoValue_VisitParams(visit, visitorToken, extraParams);
    }

    @Nullable public abstract Visit visit();
    public abstract String visitorToken();
    @Nullable public abstract Map<String, Object> extraParams();

    @Nullable public Map<String, String> utmParams() {
        Map<String, Object> extraParams = extraParams();
        if (extraParams == null) {
            return null;
        }
        Map<String, String> map = new HashMap<>();
        for (String key : utmParams) {
            if (!extraParams.containsKey(key)) {
                continue;
            }
            Object value = extraParams.get(key);
            if (value != null && value instanceof CharSequence) {
                map.put(key, value.toString());
            }
        }
        return map;
    }
}
