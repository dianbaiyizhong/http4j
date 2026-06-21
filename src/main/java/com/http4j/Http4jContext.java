package com.http4j;

import java.util.Collections;
import java.util.Map;

/**
 * Context carrying the full request state for the current HTTP call.
 * <p>
 * Obtain an instance inside any {@link ResultObserver} callback via
 * {@link Http4j#currentContext()}:
 * <pre>{@code
 * Http4jContext ctx = Http4j.currentContext();
 * String url = ctx.getUrl();
 * String method = ctx.getMethod();
 * }</pre>
 * <p>
 * The context is populated incrementally:
 * <ul>
 *   <li>Before {@link ResultObserver#callHttpStart()} — URL, method, headers, body</li>
 *   <li>After the HTTP response — status code and response body as well</li>
 * </ul>
 */
public final class Http4jContext {

    private String url;
    private String method;
    private Map<String, String> headers = Collections.emptyMap();
    private String requestBody;
    private int statusCode = -1;
    private String responseBody;

    /** Package-private constructor — created by the library. */
    Http4jContext() {
    }

    // ---- setters (package-private) ----

    void setUrl(String url) { this.url = url; }
    void setMethod(String method) { this.method = method; }
    void setHeaders(Map<String, String> headers) { this.headers = headers; }
    void setRequestBody(String requestBody) { this.requestBody = requestBody; }
    void setStatusCode(int statusCode) { this.statusCode = statusCode; }
    void setResponseBody(String responseBody) { this.responseBody = responseBody; }

    // ---- getters (public) ----

    /** Request URL. */
    public String getUrl() { return url; }

    /** HTTP method (GET, POST, PUT, DELETE, …). */
    public String getMethod() { return method; }

    /** Unmodifiable view of the request headers. */
    public Map<String, String> getHeaders() { return headers; }

    /** Request body, or {@code null} if the request has no body. */
    public String getRequestBody() { return requestBody; }

    /**
     * HTTP response status code, or {@code -1} if the request has not yet
     * received a response (e.g. inside {@link ResultObserver#callHttpStart()}).
     */
    public int getStatusCode() { return statusCode; }

    /** Response body, or {@code null} if not yet available. */
    public String getResponseBody() { return responseBody; }

    @Override
    public String toString() {
        return method + " " + url + " → " + statusCode;
    }
}
