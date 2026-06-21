package com.http4j;

/**
 * Global configuration for the {@link Http4j} SDK.
 * <p>
 * This is a mutable singleton applied to every request unless overridden per-call.
 * Set it early (e.g. at application startup) before issuing requests, to avoid
 * visibility races.
 * <p>
 * Usage:
 * <pre>{@code
 * Http4jConfig config = new Http4jConfig();
 * config.setDefaultRule(new MyBusinessRule());
 * config.setDefaultObserver(new LoggingObserver());
 * Http4j.setGlobalConfig(config);
 * }</pre>
 */
public class Http4jConfig {

    private ResultObserver defaultObserver;
    private ResultRule defaultRule;
    private JsonParser jsonParser;
    private int connectTimeout = 5000;
    private int readTimeout = 5000;

    /** Observer applied to every request that does not supply its own. */
    public ResultObserver getDefaultObserver() {
        return defaultObserver;
    }

    public void setDefaultObserver(ResultObserver defaultObserver) {
        this.defaultObserver = defaultObserver;
    }

    /** Business rule applied to every request that does not supply its own. */
    public ResultRule getDefaultRule() {
        return defaultRule;
    }

    public void setDefaultRule(ResultRule defaultRule) {
        this.defaultRule = defaultRule;
    }

    /** Connection timeout in milliseconds (default 5000). */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /** Read timeout in milliseconds (default 5000). */
    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
    /** JSON parser used to extract business code/message from response bodies. */
    public JsonParser getJsonParser() {
        return jsonParser;
    }

    public void setJsonParser(JsonParser jsonParser) {
        this.jsonParser = jsonParser;
    }
}
