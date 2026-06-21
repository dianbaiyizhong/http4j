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

    private static volatile Http4jConfig defaultConfig = new Http4jConfig();

    public Http4jConfig() {
        this.jsonParser = com.http4j.internal.JsonParsers.detect();
    }

    public static Http4jConfig getDefaultConfig() {
        return defaultConfig;
    }

    public static void setDefaultConfig(Http4jConfig cfg) {
        if (cfg == null) throw new IllegalArgumentException("default config must not be null");
        defaultConfig = cfg;
    }

    private ResultObserver defaultObserver;
    private ResultRule defaultRule;
    private JsonParser jsonParser;
    private int connectTimeout = 5000;
    private int readTimeout = 5000;
    private String baseUrl;
    private boolean ignoreSsl;

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

    /** Base URL prepended to relative request paths. */
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /** Whether to ignore SSL certificate errors. */
    public boolean isIgnoreSsl() {
        return ignoreSsl;
    }

    public void setIgnoreSsl(boolean ignoreSsl) {
        this.ignoreSsl = ignoreSsl;
    }
    /** JSON parser used to extract business code/message from response bodies. */
    public JsonParser getJsonParser() {
        return jsonParser;
    }

    public void setJsonParser(JsonParser jsonParser) {
        this.jsonParser = jsonParser;
    }
}
