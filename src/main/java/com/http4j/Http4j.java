package com.http4j;

/**
 * Entry point for the http4j HTTP request SDK.
 * <p>
 * Example usage:
 * <pre>{@code
 * String body = Http4j.request("https://api.example.com/data")
 *         .header("Authorization", "Bearer token123")
 *         .observe(new ResultObserver() {
 *             @Override
 *             public void callBusinessFail(int code, String messages) {
 *                 super.callBusinessFail(code, messages);
 *                 System.out.println("Business failed: code=" + code + " msg=" + messages);
 *             }
 *         })
 *         .executeForData();
 * }</pre>
 */
public final class Http4j {

    private static volatile Http4jConfig globalConfig = new Http4jConfig();

    private Http4j() {
    }

    /**
     * Create a new HTTP request builder for the given URL.
     *
     * @param url the target URL (http or https)
     * @return a new {@link Http4jRequest} instance
     */
    public static Http4jRequest request(String url) {
        return new Http4jRequest(url);
    }

    /**
     * Replace the entire global configuration.
     * <p>
     * Call this once at application startup to set defaults for all requests.
     *
     * @param config the new configuration (must not be null)
     */
    public static void setGlobalConfig(Http4jConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("global config must not be null");
        }
        globalConfig = config;
    }

    /**
     * Returns the current global configuration (mutable; non-null).
     */
    public static Http4jConfig getGlobalConfig() {
        return globalConfig;
    }
}
