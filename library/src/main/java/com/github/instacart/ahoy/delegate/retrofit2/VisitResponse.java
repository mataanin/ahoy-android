package com.github.instacart.ahoy.delegate.retrofit2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class VisitResponse {

    @JsonCreator
    public static VisitResponse create(@JsonProperty("visit_id") String visitId) {
        return new AutoValue_VisitResponse(visitId);
    }

    public abstract String visitId();
}
