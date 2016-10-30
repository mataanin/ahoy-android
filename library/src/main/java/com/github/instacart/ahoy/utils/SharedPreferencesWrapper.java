package com.github.instacart.ahoy.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SharedPreferencesWrapper {

    private final SharedPreferences mSharedPreferences;

    public SharedPreferencesWrapper(Context context, String fileName) {
        mSharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
    }

    private String getMapFieldKey(String mapKey, String fieldKey) {
        return String.format("%s_%s", mapKey, fieldKey);
    }

    public void delete(String key) {
        mSharedPreferences.edit().remove(key).apply();
    }

    public void putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void putString(String key, String value, boolean commit) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        if (commit) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    public void putString(String key, String value) {
        putString(key, value, false);
    }

    public void putStringSet(String key, Set<String> value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putStringSet(key, value);
        editor.apply();
    }

    public Set<String> getStringSet(String key, Set<String> defaultValue) {
        return mSharedPreferences.getStringSet(key, defaultValue);
    }

    public void putInteger(String key, Integer value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public Integer getInteger(String key, Integer defaultValue) {
        return mSharedPreferences.getInt(key, defaultValue);
    }

    public long getLong(String key, long defaultValue) {
        return mSharedPreferences.getLong(key, defaultValue);
    }

    public void putLong(String key, long timestamp) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putLong(key, timestamp);
        editor.apply();
    }

    public String getString(String key, String defaultValue) {
        return mSharedPreferences.getString(key, defaultValue);
    }

    public void putStringMap(String mapKey, @Nullable Map<String, String> map) {
        Editor editor = mSharedPreferences.edit();

        if (map == null) {
            editor.remove(mapKey);
            editor.apply();
            return;
        }

        for (String fieldKey : map.keySet()) {
            editor.putString(getMapFieldKey(mapKey, fieldKey), map.get(fieldKey));
        }
        editor.putStringSet(mapKey, map.keySet());
        editor.commit();
    }

    public Map<String, String> getStringMap(String mapKey, String fieldDefault, Map<String, String> defaultValue) {
        Set<String> fieldKeys = getStringSet(mapKey, null);
        if (TypeUtil.isEmpty(fieldKeys)) {
            return defaultValue;
        }
        Map<String, String> map = new HashMap<>();
        for (String fieldKey : fieldKeys) {
            map.put(fieldKey, getString(getMapFieldKey(mapKey, fieldKey), fieldDefault));
        }
        return map;
    }

    public void clear() {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.clear();
        edit.apply();
    }
}