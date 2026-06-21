package com.http4j;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.http4j.internal.ResultObserverHelper;

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
    private JsonParser jsonParser;
    private int connectTimeout;
    private int readTimeout;
    Http4jRequest(String url, Http4jConfig config) {
        this.url = url;
        this.connectTimeout = config.getConnectTimeout();
        this.readTimeout = config.getReadTimeout();
        this.observer = config.getDefaultObserver();
        this.rule = config.getDefaultRule();
        this.jsonParser = config.getJsonParser();
    }

    // ---- builder methods ----

    /**
     * Set the HTTP method (GET, POST, PUT, DELETE, etc.). Default is GET.
     */
    public Http4jRequest method(String method) {
        this.method = method.toUpperCase();
        return this;
    }

    /**
     * Add a request header.
     */
    public Http4jRequest header(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    /**
     * Set the request body (for POST/PUT etc.).
     */
    public Http4jRequest body(String body) {
        this.requestBody = body;
        return this;
    }

    /**
     * Attach a per-request observer.
     * <p>
     * For each lifecycle callback the library checks whether the local observer
     * overrides that method:
     * <ul>
     *   <li><strong>overridden</strong> → only the local callback runs
     *       (the global default does <em>not</em> run automatically)</li>
     *   <li><strong>not overridden</strong> → the global default callback runs</li>
     * </ul>
     * <p>
     * If you want to opt in to the global default behaviour inside an overridden
     * method, call {@link #defaultObserver()} explicitly:
     * <pre>{@code
     * defaultObserver().callHttpFail(code, msg, ex);
     * }</pre>
     */
    public Http4jRequest observe(ResultObserver observer) {
        if (observer == null) {
            return this;
        }
        if (this.observer != null) {
            final ResultObserver global = this.observer;
            final ResultObserver local = observer;

            ResultObserverHelper.setParent(local, global);

            boolean overriddenHttpStart = isOverridden(local, "callHttpStart");
            boolean overriddenHttpSucc = isOverridden(local, "callHttpSuccess");
            boolean overriddenHttpFail = isOverridden(local, "callHttpFail", int.class, String.class, Throwable.class);
            boolean overriddenBizSucc = isOverridden(local, "callBusinessSuccess");
            boolean overriddenBizFail = isOverridden(local, "callBusinessFail", int.class, String.class);

            this.observer = new ResultObserver() {
                @Override
                public void callHttpStart() {
                    if (overriddenHttpStart) local.callHttpStart();
                    else global.callHttpStart();
                }

                @Override
                public void callHttpSuccess() {
                    if (overriddenHttpSucc) local.callHttpSuccess();
                    else global.callHttpSuccess();
                }

                @Override
                public void callHttpFail(int statusCode, String message, Throwable throwable) {
                    if (overriddenHttpFail) local.callHttpFail(statusCode, message, throwable);
                    else global.callHttpFail(statusCode, message, throwable);
                }

                @Override
                public void callBusinessSuccess() {
                    if (overriddenBizSucc) local.callBusinessSuccess();
                    else global.callBusinessSuccess();
                }

                @Override
                public void callBusinessFail(int code, String message) {
                    if (overriddenBizFail) local.callBusinessFail(code, message);
                    else global.callBusinessFail(code, message);
                }
            };
        } else {
            this.observer = observer;
        }
        return this;
    }

    /**
     * Set a per-request business rule, replacing the global default rule
     * set via {@link Http4jConfig#setDefaultRule(ResultRule)}.
     */
    public Http4jRequest rule(ResultRule rule) {
        if (rule == null) {
            return this;
        }
        this.rule = rule;
        return this;
    }

    /**
     * Set the default observer for this request.
     */
    public Http4jRequest setDefaultObserver(ResultObserver observer) {
        if (observer != null) {
            this.observer = observer;
        }
        return this;
    }

    /**
     * Set the default rule for this request.
     */
    public Http4jRequest setDefaultRule(ResultRule rule) {
        if (rule != null) {
            this.rule = rule;
        }
        return this;
    }

    /**
     * Override the connection timeout (milliseconds) for this request.
     */
    public Http4jRequest connectTimeout(int millis) {
        this.connectTimeout = millis;
        return this;
    }

    /**
     * Override the read timeout (milliseconds) for this request.
     */
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
    public String execute() {
        Http4jContext ctx = new Http4jContext();
        ctx.setUrl(url);
        ctx.setMethod(method);
        ctx.setHeaders(headers);
        ctx.setRequestBody(requestBody);
        Http4j.contextHolder.set(ctx);

        try {
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

                ctx.setStatusCode(statusCode);
                ctx.setResponseBody(body);

                boolean httpOk = statusCode >= 200 && statusCode < 400;
                if (httpOk) {
                    fireHttpSuccess();
                    evaluateBusiness(body);
                } else {
                    fireHttpFail(statusCode, "HTTP " + statusCode + ": " + body, null);
                }

                return rule != null ? rule.getBusinessData(body) : body;

            } catch (Exception e) {
                int code = 0;
                if (conn != null) {
                    try {
                        code = conn.getResponseCode();
                    } catch (Exception ignored) {
                    }
                }
                ctx.setStatusCode(code);
                fireHttpFail(code, e.getMessage(), e);
                return "";
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
                Http4j.contextHolder.remove();
            }
        } finally {
            Http4j.contextHolder.remove();
        }
    }

    /**
     * Execute the request and deserialize the response body into the specified type.
     * <p>
     * Requires a {@link JsonParser} to be configured via {@link Http4jConfig#setJsonParser(JsonParser)}.
     * <p>
     * Fires the observer lifecycle and evaluates the business rule internally, same as
     * {@link #execute()}.
     *
     * @param clazz the target class for deserialization
     * @param <T>   the target type
     * @return the deserialized response object, or {@code null} if the body is empty
     * @throws IllegalStateException if no {@link JsonParser} is configured
     */
    public <T> T execute(Class<T> clazz) {
        String body = execute();
        if (jsonParser != null) {
            return jsonParser.parse(body, clazz);
        }
        return null;
    }

    // ---- internal ----
    private void evaluateBusiness(String body) {
        if (rule == null) {
            return;
        }
        if (rule.isBusinessSuccess(body)) {
            fireBusinessSuccess();
            Http4j.currentContext().setBusinessData(rule.getBusinessData(body));
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

    /**
     * Returns {@code true} if the observer's class declares the given method
     * (i.e. the method was overridden in a subclass).
     */
    private static boolean isOverridden(ResultObserver observer, String methodName, Class<?>... paramTypes) {
        Class<?> clazz = observer.getClass();
        if (clazz == ResultObserver.class) {
            return false;
        }
        try {
            return clazz.getMethod(methodName, paramTypes).getDeclaringClass() != ResultObserver.class;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
