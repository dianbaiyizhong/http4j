package com.http4j;

/**
 * Defines how to interpret the business-level result from an HTTP response body.
 * <p>
 * The default implementation treats a JSON payload with {@code "code":0} as a business success.
 * Custom rules can be set globally via {@link Http4jConfig} or per-request via {@link Http4jRequest#rule(ResultRule)}.
 */
public interface ResultRule {

    /**
     * Determine whether the response body represents a business success.
     */
    boolean isBusinessSuccess(String body);

    /**
     * Extract the business code from the response body.
     * <p>
     * Default returns {@code -1}. Override to provide custom extraction logic.
     */
    default int getBusinessCode(String body) {
        return -1;
    }

    /**
     * Extract the business message from the response body.
     * <p>
     * Default returns empty string. Override to provide custom extraction logic.
     */
    default String getBusinessMessage(String body) {
        return "";
    }


    /**
     * Extract the business data payload from the full response body.
     * <p>
     * Many APIs wrap the actual data in a nested structure:
     * <pre>{@code
     * { "code":0, "message":"ok", "data":{"id":1,"name":"Alice"} }
     * }</pre>
     * Override this method to return only the inner payload (e.g. the "data" field),
     * which can then be deserialized by {@link JsonParser#parse(String, Class)}.
     * <p>
     * The default implementation returns the full body unchanged.
     *
     * @param body the full HTTP response body
     * @return the business data payload as a string
     */
    default String getBusinessData(String body) {
        return body;
    }
}
