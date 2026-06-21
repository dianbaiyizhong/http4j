package com.http4j;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * A builder for a single HTTP request.
 * <p>
 * Obtain an instance via {@link Http4j#request(String)} then configure and execute:
 * <pre>{@code
 * String body = Http4j.request("https://api.example.com/users")
 *         .header("Authorization", "Bearer xxx")
 *         .observe(new MyObserver())
 *         .executeForData();
 * }</pre>
 */
public class Http4jRequest {

    private final String url;
    private String method = "GET";
    private final Map<String, String> headers = new HashMap<>();
    private String requestBody;
    private ResultObserver observer;
    private ResultRule rule;
    private int connectTimeout;
    private int readTimeout;
    private boolean useGlobalRule = true;

    Http4jRequest(String url) {
        this.url = url;
        Http4jConfig cfg = Http4j.getGlobalConfig();
        this.connectTimeout = cfg.getConnectTimeout();
        this.readTimeout = cfg.getReadTimeout();
        this.observer = cfg.getDefaultObserver();
        this.rule = cfg.getDefaultRule();
    }

    // ---- builder methods ----

    /** Set the HTTP method (GET, POST, PUT, DELETE, etc.). Default is GET. */
    public Http4jRequest method(String method) {
        this.method = method.toUpperCase();
        return this;
    }

    /** Add a request header. */
    public Http4jRequest header(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    /** Set the request body (for POST/PUT etc.). */
    public Http4jRequest body(String body) {
        this.requestBody = body;
        return this;
    }

    /**
     * Attach a per-request observer.
     * <p>
     * The given observer always <strong>chains on top of</strong> the global default observer
     * set via {@link Http4jConfig#setDefaultObserver(ResultObserver)}:
     * the global callback runs first, then the local callback.
     * <p>
     * If no global observer has been set, the local observer is used directly.
     */
    public Http4jRequest observe(ResultObserver observer) {
        if (observer == null) {
            return this;
        }
        if (this.observer != null) {
            // chain: global → local
            final ResultObserver global = this.observer;
            final ResultObserver local = observer;
            this.observer = new ResultObserver() {
                @Override
                public void callHttpStart() {
                    global.callHttpStart();
                    local.callHttpStart();
                }

                @Override
                public void callHttpSuccess() {
                    global.callHttpSuccess();
                    local.callHttpSuccess();
                }

                @Override
                public void callHttpFail(int statusCode, String message, Throwable throwable) {
                    global.callHttpFail(statusCode, message, throwable);
                    local.callHttpFail(statusCode, message, throwable);
                }

                @Override
                public void callBusinessSuccess() {
                    global.callBusinessSuccess();
                    local.callBusinessSuccess();
                }

                @Override
                public void callBusinessFail(int code, String message) {
                    global.callBusinessFail(code, message);
                    local.callBusinessFail(code, message);
                }
            };
        } else {
            this.observer = observer;
        }
        return this;
    }

    /**
     * Set a per-request business rule.
     * <p>
     * By default the rule <strong>wraps</strong> the global default rule
     * (the global rule acts as a pre-filter). Call {@link #overrideGlobalRule()}
     * before this method to replace the global rule entirely.
     *
     * @see #overrideGlobalRule()
     */
    public Http4jRequest rule(ResultRule rule) {
        if (rule == null) {
            return this;
        }
        if (useGlobalRule && this.rule != null) {
            // composite: global first, then local
            final ResultRule global = this.rule;
            final ResultRule local = rule;
            this.rule = new ResultRule() {
                @Override
                public boolean isBusinessSuccess(String body) {
                    return global.isBusinessSuccess(body) && local.isBusinessSuccess(body);
                }

                @Override
                public int getBusinessCode(String body) {
                    return local.getBusinessCode(body);
                }

                @Override
                public String getBusinessMessage(String body) {
                    return local.getBusinessMessage(body);
                }
            };
        } else {
            this.rule = rule;
        }
        this.useGlobalRule = false;
        return this;
    }

    /**
     * When set before {@link #rule(ResultRule)}, the per-request rule
     * <em>replaces</em> (rather than layers on top of) the global default rule.
     */
    public Http4jRequest overrideGlobalRule() {
        this.useGlobalRule = false;
        return this;
    }

    /** Override the connection timeout (milliseconds) for this request. */
    public Http4jRequest connectTimeout(int millis) {
        this.connectTimeout = millis;
        return this;
    }

    /** Override the read timeout (milliseconds) for this request. */
    public Http4jRequest readTimeout(int millis) {
        this.readTimeout = millis;
        return this;
    }

    // ---- execution ----

    /**
     * Execute the request and return the response body as a raw string.
     * <p>
     * Fires the observer lifecycle and evaluates the business rule internally.
     */
    public String executeForData() {
        fireHttpStart();

        HttpURLConnection conn = null;
        try {
            URL dest = new URL(url);
            conn = (HttpURLConnection) dest.openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(connectTimeout);
            conn.setReadTimeout(readTimeout);
            conn.setDoInput(true);

            // headers
            for (Map.Entry<String, String> h : headers.entrySet()) {
                conn.setRequestProperty(h.getKey(), h.getValue());
            }

            // body
            if (requestBody != null && !requestBody.isEmpty()) {
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(requestBody.getBytes(StandardCharsets.UTF_8));
                }
            }

            int statusCode = conn.getResponseCode();
            String body = readBody(conn, statusCode);

            boolean httpOk = statusCode >= 200 && statusCode < 400;
            if (httpOk) {
                fireHttpSuccess();
                evaluateBusiness(body);
            } else {
                fireHttpFail(statusCode, "HTTP " + statusCode + ": " + body, null);
            }

            return body;

        } catch (Exception e) {
            int code = 0;
            if (conn != null) {
                try {
                    code = conn.getResponseCode();
                } catch (Exception ignored) {
                }
            }
            fireHttpFail(code, e.getMessage(), e);
            return "";
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    // ---- internal ----

    private void evaluateBusiness(String body) {
        if (rule == null) {
            return;
        }
        if (rule.isBusinessSuccess(body)) {
            fireBusinessSuccess();
        } else {
            int code = rule.getBusinessCode(body);
            String msg = rule.getBusinessMessage(body);
            fireBusinessFail(code, msg);
        }
    }

    private String readBody(HttpURLConnection conn, int statusCode) throws IOException {
        InputStream is = statusCode >= 400 ? conn.getErrorStream() : conn.getInputStream();
        if (is == null) {
            return "";
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private void fireHttpStart() {
        if (observer != null) {
            observer.callHttpStart();
        }
    }

    private void fireHttpSuccess() {
        if (observer != null) {
            observer.callHttpSuccess();
        }
    }

    private void fireHttpFail(int code, String message, Throwable throwable) {
        if (observer != null) {
            observer.callHttpFail(code, message, throwable);
        }
    }

    private void fireBusinessSuccess() {
        if (observer != null) {
            observer.callBusinessSuccess();
        }
    }

    private void fireBusinessFail(int code, String message) {
        if (observer != null) {
            observer.callBusinessFail(code, message);
        }
    }

}
