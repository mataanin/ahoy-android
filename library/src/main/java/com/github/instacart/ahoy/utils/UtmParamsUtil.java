package com.github.instacart.ahoy.utils;

import android.net.Uri;
import android.support.annotation.Nullable;

import com.github.instacart.ahoy.Visit;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class UtmParamsUtil {

    private static final String[] utmParams = {
            Visit.UTM_CAMPAIGN,
            Visit.UTM_CONTENT,
            Visit.UTM_MEDIUM,
            Visit.UTM_SOURCE,
            Visit.UTM_TERM
    };

    private UtmParamsUtil() {
    }

    @Nullable public static Map<String, String> utmParams(@Nullable  Map<String, Object> params) {
        if (params == null) {
            return null;
        }
        Map<String, String> map = new HashMap<>();
        for (String key : utmParams) {
            if (!params.containsKey(key)) {
                continue;
            }
            Object value = params.get(key);
            if (value != null && value instanceof CharSequence) {
                map.put(key, value.toString());
            }
        }
        return map;
    }

    public static Uri utmUri(Map<String, Object> params) {
        if (params == null) {
            return null;
        }
        Uri.Builder builder = new Uri.Builder();
        Set<Entry<String, String>> utmParams = UtmParamsUtil.utmParams(params).entrySet();
        for (Map.Entry<String, String> entry : utmParams) {
            builder = builder.appendQueryParameter(entry.getKey(), Uri.encode(entry.getValue()));
        }
        return builder.build();
    }
}
