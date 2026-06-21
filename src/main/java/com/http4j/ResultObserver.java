package com.http4j;

import com.http4j.internal.ResultObserverHelper;

/**
 * Lifecycle observer for a single HTTP request.
 * <p>
 * Implement (or use an anonymous class) to receive callbacks during request
 * execution. Every method is a {@code default} no-op, so you only override the
 * ones you care about.
 * <p>
 * Inside any callback you can access the full request context via
 * {@link Http4j#currentContext()}:
 * <pre>{@code
 * Http4jContext ctx = Http4j.currentContext();
 * String url = ctx.getUrl();
 * String method = ctx.getMethod();
 * }</pre>
 * <p>
 * <strong>Invocation order:</strong>
 * <ol>
 *   <li>{@link #callHttpStart()}</li>
 *   <li>{@link #callHttpSuccess()} <em>or</em> {@link #callHttpFail(int, String, Throwable)}</li>
 *   <li>If HTTP succeeded, the configured {@link ResultRule} is evaluated:
 *       <ul>
 *         <li>business success → {@link #callBusinessSuccess()}</li>
 *         <li>business failure → {@link #callBusinessFail(int, String)}</li>
 *       </ul>
 *   </li>
 * </ol>
 * <p>
 * <strong>Overriding vs inheriting the global default observer</strong>
 * (set via {@link Http4jConfig#setDefaultObserver(ResultObserver)}):
 * <ul>
 *   <li>If you override a method → the global default does <em>not</em> run for that method.
 *       (Use {@link #defaultObserver()} to invoke it explicitly.)</li>
 *   <li>If you do not override a method → the global default runs.</li>
 * </ul>
 *
 * @see Http4jConfig#setDefaultObserver(ResultObserver)
 * @see Http4jRequest#observe(ResultObserver)
 * @see Http4j#currentContext()
 * @see Http4jContext
 */
public interface ResultObserver {

    /** Called immediately before the HTTP connection is opened. */
    default void callHttpStart() {
    }

    /** Called after a successful HTTP response (any 2xx / 3xx status). */
    default void callHttpSuccess() {
    }

    /**
     * Called when the HTTP request fails (network error, non-2xx/3xx status, etc.).
     *
     * @param statusCode HTTP status code, or 0 if a transport exception occurred
     * @param message    error description
     * @param throwable  the original exception, or {@code null} if the failure is an HTTP-level
     *                   error (non-2xx/3xx status) rather than a transport exception
     */
    default void callHttpFail(int statusCode, String message, Throwable throwable) {
    }

    /**
     * Called when the HTTP response is received and the {@link ResultRule} judges it a
     * business success (e.g. JSON {@code code == 0}).
     */
    default void callBusinessSuccess() {
    }

    /**
     * Called when the HTTP response is received but the {@link ResultRule} judges it a
     * business failure.
     *
     * @param code    business error code from the response
     * @param message business error message from the response
     */
    default void callBusinessFail(int code, String message) {
    }

    /**
     * Access the global default observer, if one was configured via
     * {@link Http4jConfig#setDefaultObserver(ResultObserver)}.
     * <p>
     * Call this from an overridden callback to explicitly invoke the global
     * default behaviour:
     * <pre>{@code
     * ResultObserver.super.defaultObserver().callBusinessFail(code, msg);
     * }</pre>
     *
     * @return the default observer, or {@code null} if none was set
     */
    default ResultObserver defaultObserver() {
        return ResultObserverHelper.getParent(this);
    }
}
