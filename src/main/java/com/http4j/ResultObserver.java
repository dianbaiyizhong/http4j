package com.http4j;

/**
 * Lifecycle observer for a single HTTP request.
 * <p>
 * Extend this class to receive callbacks during request execution.
 * Every method has a default no-op implementation, so you only override
 * the ones you care about.
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
 *         <li>business failure → {@link #callBusinessFail()}</li>
 *       </ul>
 *   </li>
 * </ol>
 *
 * @see Http4jConfig#setDefaultObserver(ResultObserver)
 * @see Http4jRequest#observe(ResultObserver)
 * @see Http4j#currentContext()
 * @see Http4jContext
 */
public abstract class ResultObserver {

    /**
     * Called immediately before the HTTP connection is opened.
     */
    public void callHttpStart() {
    }

    /**
     * Called after a successful HTTP response (any 2xx / 3xx status).
     */
    public void callHttpSuccess() {
    }

    /**
     * Called when the HTTP request fails (network error, non-2xx/3xx status, etc.).
     *
     * @param statusCode HTTP status code, or 0 if a transport exception occurred
     * @param message    error description
     * @param throwable  the original exception, or {@code null} if the failure is an HTTP-level
     *                   error (non-2xx/3xx status) rather than a transport exception
     */
    public void callHttpFail(int statusCode, String message, Throwable throwable) {
        throw new Http4jException("HTTP request failed: ", throwable);
    }

    /**
     * Called to determine whether the response is a business success.
     * <p>
     * Return {@code true} to indicate a business success. The default implementation
     * checks for {@code "code":0} in the response body (via {@link Http4j#currentContext()}).
     *
     */
    public void callBusinessSuccess() {
    }

    /**
     * Called when {@link #callBusinessSuccess()} returned {@code false}.
     * <p>
     * Return {@code true} to indicate a business failure.
     *
     * @return {@code true} if this is a business failure
     */
    public boolean callBusinessFail() {
        return true;
    }

    /**
     * Access the global default observer, if one was configured via
     * {@link Http4jConfig#setDefaultObserver(ResultObserver)}.
     * <p>
     * Call this from an overridden callback to explicitly invoke the global
     * default behaviour:
     * <pre>{@code
     * super.defaultObserver().callBusinessFail(code, msg);
     * }</pre>
     *
     * @return the default observer, or {@code null} if none was set
     */
    public ResultObserver defaultObserver() {
        return ResultObserverHelper.getParent(this);
    }
}
