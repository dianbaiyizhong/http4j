package com.http4j;

/**
 * Entry point for the http4j HTTP request SDK.
 * <p>
 * Each instance carries its own {@link Http4jConfig}, so you can create
 * multiple instances with different configurations without interference.
 * <p>
 * Example usage:
 * <pre>{@code
 * Http4jConfig cfg = new Http4jConfig();
 * cfg.setDefaultRule(new DefaultResultRule());
 *
 * Http4j http4j = new Http4j(cfg);
 * String body = http4j.request("https://api.example.com/data")
 *         .header("Authorization", "Bearer token123")
 *         .observe(new ResultObserver() {
 *             public void callBusinessFail(int code, String messages) {
 *                 Http4jContext ctx = Http4j.currentContext();
 *                 System.out.println("Business failed: code=" + code + " msg=" + messages);
 *             }
 *         })
 *         .executeForData();
 * }</pre>
 */
public class Http4j {

    /** Thread-local request context, set before each callback invocation. */
    static final ThreadLocal<Http4jContext> contextHolder = new ThreadLocal<>();

    private final Http4jConfig config;

    /**
     * Create an instance with default configuration.
     */
    public Http4j() {
        this.config = Http4jConfig.getDefaultConfig();
    }

    /**
     * Create an instance with a custom configuration.
     *
     * @param config the configuration to use (must not be null)
     */
    public Http4j(Http4jConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        this.config = config;
    }

    /**
     * Returns the configuration used by this instance.
     */
    public Http4jConfig getConfig() {
        return config;
    }

    /**
     * Create a new HTTP request builder using the global default configuration.
     *
     * @param url the target URL (http or https)
     * @return a new {@link Http4jRequest} instance
     */
    public static Http4jRequest request(String url) {
        return new Http4jRequest(url, getDefaultConfig());
    }

    /**
     * Set the global default configuration used by all {@code new Http4j()} instances
     * that do not receive an explicit config.
     */
    public static void setDefaultConfig(Http4jConfig cfg) {
        Http4jConfig.setDefaultConfig(cfg);
    }

    /**
     * Returns the current global default configuration.
     */
    public static Http4jConfig getDefaultConfig() {
        return Http4jConfig.getDefaultConfig();
    }

    /**
     * Returns the {@link Http4jContext} for the current request thread,
     * or {@code null} if called outside of an active request.
     * <p>
     * This is the primary way for {@link ResultObserver} implementations to
     * access request details (URL, method, headers, etc.).
     */
    public static Http4jContext currentContext() {
        return contextHolder.get();
    }
}
