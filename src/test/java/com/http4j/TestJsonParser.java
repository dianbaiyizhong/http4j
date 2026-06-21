package com.http4j;

import java.util.*;

public class TestJsonParser implements JsonParser {

    @Override
    public Object parse(String json) {
        if (json == null || json.trim().isEmpty() || !json.trim().startsWith("{")) {
            return null;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        String s = json.trim();
        s = s.substring(1, s.length() - 1);
        if (s.trim().isEmpty()) return map;

        for (String pair : s.split(",")) {
            String[] kv = pair.split(":", 2);
            if (kv.length != 2) continue;
            String key = kv[0].trim().replaceAll("^\"|\"$", "");
            String val = kv[1].trim().replaceAll("^\"|\"$", "");
            try { map.put(key, Integer.parseInt(val)); }
            catch (NumberFormatException e) { map.put(key, val); }
        }
        return map;
    }

    @Override
    public <T> T parse(String json, Class<T> clazz) {
        throw new UnsupportedOperationException(
            "TestJsonParser does not support typed parsing");
    }
}
