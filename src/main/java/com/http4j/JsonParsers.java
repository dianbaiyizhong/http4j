package com.http4j;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Auto-detects popular JSON libraries on the classpath via reflection.
 * <p>
 * Search order: Gson → Jackson → Fastjson.
 * Returns {@code null} if no supported library is found.
 */
public final class JsonParsers {

    private JsonParsers() {}

    public static JsonParser detect() {
        JsonParser p = tryGson();   if (p != null) return p;
        p = tryJackson();           if (p != null) return p;
        p = tryFastjson();          if (p != null) return p;
        return null;
    }

    private static JsonParser tryGson() {
        try {
            Class<?> cls = Class.forName("com.google.gson.Gson");
            Object gson = cls.getConstructor().newInstance();
            Method fromJson = cls.getMethod("fromJson", String.class, Class.class);
            return new JsonParser() {
                @Override public Object parse(String json) {
                    try { return fromJson.invoke(gson, json, Map.class); }
                    catch (Exception e) { return null; }
                }
                @Override @SuppressWarnings("unchecked")
                public <T> T parse(String json, Class<T> clazz) {
                    try { return (T) fromJson.invoke(gson, json, clazz); }
                    catch (Exception e) { return null; }
                }
            };
        } catch (Exception e) { return null; }
    }

    private static JsonParser tryJackson() {
        try {
            Class<?> cls = Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
            Object mapper = cls.getConstructor().newInstance();
            Method readValue = cls.getMethod("readValue", String.class, Class.class);
            return new JsonParser() {
                @Override public Object parse(String json) {
                    try { return readValue.invoke(mapper, json, Map.class); }
                    catch (Exception e) { return null; }
                }
                @Override @SuppressWarnings("unchecked")
                public <T> T parse(String json, Class<T> clazz) {
                    try { return (T) readValue.invoke(mapper, json, clazz); }
                    catch (Exception e) { return null; }
                }
            };
        } catch (Exception e) { return null; }
    }

    private static JsonParser tryFastjson() {
        try {
            Class<?> cls = Class.forName("com.alibaba.fastjson2.JSON");
            Method parseObject = cls.getMethod("parseObject", String.class, Class.class);
            return new JsonParser() {
                @Override public Object parse(String json) {
                    try { return parseObject.invoke(null, json, Map.class); }
                    catch (Exception e) { return null; }
                }
                @Override @SuppressWarnings("unchecked")
                public <T> T parse(String json, Class<T> clazz) {
                    try { return (T) parseObject.invoke(null, json, clazz); }
                    catch (Exception e) { return null; }
                }
            };
        } catch (Exception e) {
            // try fastjson 1.x
            try {
                Class<?> cls = Class.forName("com.alibaba.fastjson.JSON");
                Method parseObject = cls.getMethod("parseObject", String.class, Class.class);
                return new JsonParser() {
                    @Override public Object parse(String json) {
                        try { return parseObject.invoke(null, json, Map.class); }
                        catch (Exception e2) { return null; }
                    }
                    @Override @SuppressWarnings("unchecked")
                    public <T> T parse(String json, Class<T> clazz) {
                        try { return (T) parseObject.invoke(null, json, clazz); }
                        catch (Exception e2) { return null; }
                    }
                };
            } catch (Exception e2) {
                return null; }
        }
    }
}
