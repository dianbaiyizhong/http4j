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
     * Extract the business code from the response body. Called when {@link #isBusinessSuccess} returns false.
     */
    int getBusinessCode(String body);

    /**
     * Extract the business message from the response body. Called when {@link #isBusinessSuccess} returns false.
     */
    String getBusinessMessage(String body);
}
