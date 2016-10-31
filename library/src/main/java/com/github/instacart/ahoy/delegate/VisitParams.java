package com.github.instacart.ahoy.delegate;

import android.support.annotation.Nullable;

import com.github.instacart.ahoy.Visit;
import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class VisitParams {

    public static VisitParams create(String visitorToken, @Nullable Visit visit,
            @Nullable Map<String, Object> extraParams) {
        return new AutoValue_VisitParams(visit, visitorToken, extraParams);
    }

    @Nullable public abstract Visit visit();
    public abstract String visitorToken();
    @Nullable public abstract Map<String, Object> extraParams();
}
