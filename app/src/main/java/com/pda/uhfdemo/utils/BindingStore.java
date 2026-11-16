package com.pda.uhfdemo.utils;

import com.pda.uhfdemo.entity.BindInfo;
import com.tencent.mmkv.MMKV;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BindingStore {
    private static final String PREFIX = "rfid_bind_";
    private static final MMKV STORE = MMKV.defaultMMKV();

    private BindingStore() {
    }

    public static synchronized Map<String, String> load() {
        LinkedHashMap<String, String> bindings = new LinkedHashMap<>();
        String[] keys = STORE.allKeys();
        if (keys == null) {
            return bindings;
        }

        for (String key : keys) {
            if (key.startsWith(PREFIX)) {
                String id = key.substring(PREFIX.length());
                String name = STORE.decodeString(key, null);
                if (!id.isEmpty() && name != null) {
                    bindings.put(id, name);
                }
            }
        }
        for (String key : keys) {
            if (!key.startsWith(PREFIX) && isLegacyTagId(key) && !bindings.containsKey(key)) {
                String name = STORE.decodeString(key, null);
                if (name != null) {
                    bindings.put(key, name);
                    STORE.encode(PREFIX + key, name);
                }
            }
        }
        return bindings;
    }

    public static synchronized boolean contains(String id) {
        return id != null && load().containsKey(id);
    }

    public static synchronized boolean save(String id, String name) {
        return id != null && !id.isEmpty() && name != null
                && STORE.encode(PREFIX + id, name);
    }

    public static synchronized boolean removeAll() {
        String[] keys = STORE.allKeys();
        if (keys == null) {
            return true;
        }
        for (String key : keys) {
            if (key.startsWith(PREFIX) || isLegacyTagId(key)) {
                STORE.remove(key);
                if (STORE.containsKey(key)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static synchronized List<BindInfo> loadBindInfo() {
        List<BindInfo> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : load().entrySet()) {
            result.add(new BindInfo(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    private static boolean isLegacyTagId(String key) {
        if (key.isEmpty()) {
            return false;
        }
        for (int i = 0; i < key.length(); i++) {
            char character = key.charAt(i);
            if (!((character >= '0' && character <= '9')
                    || (character >= 'a' && character <= 'f')
                    || (character >= 'A' && character <= 'F'))) {
                return false;
            }
        }
        return true;
    }
}
