package com.github.instacart.ahoy;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Visit {

    public static Visit create(String visitToken, long expiresAt) {
        return new AutoValue_Visit(visitToken, expiresAt);
    }

    public abstract String visitToken();
    public abstract long expiresAt();

    public boolean isValid() {
        return System.currentTimeMillis() < expiresAt();
    }
}
