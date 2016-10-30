package com.github.instacart.ahoy.delegate;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class VisitParams {

    public static VisitParams create(String visitorToken, @Nullable String visitToken,
            @Nullable Map<String, String> extraParams) {
        return new AutoValue_VisitParams(visitToken, visitorToken, extraParams);
    }

    @Nullable public abstract String visitToken();
    public abstract String visitorToken();
    @Nullable public abstract Map<String, String> extraParams();
}
