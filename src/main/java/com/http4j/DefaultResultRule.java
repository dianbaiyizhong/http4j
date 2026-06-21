package com.http4j;

import com.http4j.internal.JsonUtil;

import java.util.Map;

/**
 * Default business-rule implementation that interprets a JSON response body.
 * <p>
 * Expects the body to be a JSON object with at least a numeric {@code "code"} field.
 * {@code code == 0} is treated as business success; any other value is a business failure.
 */
public class DefaultResultRule implements ResultRule {

    @Override
    public boolean isBusinessSuccess(String body) {
        if (body == null || body.isEmpty()) {
            return false;
        }
        try {
            Object parsed = JsonUtil.parse(body);
            if (parsed instanceof Map) {
                Object code = ((Map<?, ?>) parsed).get("code");
                if (code instanceof Number) {
                    return ((Number) code).intValue() == 0;
                }
            }
        } catch (Exception ignored) {
            // not valid JSON – treat as http-success but not a business call
        }
        return false;
    }

    @Override
    public int getBusinessCode(String body) {
        if (body == null || body.isEmpty()) {
            return -1;
        }
        try {
            Object parsed = JsonUtil.parse(body);
            if (parsed instanceof Map) {
                Object code = ((Map<?, ?>) parsed).get("code");
                if (code instanceof Number) {
                    return ((Number) code).intValue();
                }
            }
        } catch (Exception ignored) {
        }
        return -1;
    }

    @Override
    public String getBusinessMessage(String body) {
        if (body == null || body.isEmpty()) {
            return "";
        }
        try {
            Object parsed = JsonUtil.parse(body);
            if (parsed instanceof Map) {
                Object msg = ((Map<?, ?>) parsed).get("message");
                if (msg != null) {
                    return msg.toString();
                }
            }
        } catch (Exception ignored) {
        }
        return "";
    }
}
