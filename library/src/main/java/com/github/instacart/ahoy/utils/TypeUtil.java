package com.github.instacart.ahoy.utils;

public class TypeUtil {

    private TypeUtil() {
    }

    public static boolean isEmpty(String value) {
        return value == null || value.length() == 0;
    }
}
