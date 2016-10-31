package com.github.instacart.ahoy.utils;

import java.util.Collection;
import java.util.Map;

public class TypeUtil {

    private TypeUtil() {
    }

    public static boolean isEmpty(CharSequence value) {
        return value == null || value.length() == 0;
    }

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.size() == 0;
    }

    public static boolean isEmpty(Map map) {
        return map == null || map.size() == 0;
    }

    public static <T> T ifNull(T object, T alternative) {
        return object != null ? object : alternative;
    }
}
