package com.http4j;

/**
 * Observer for the HTTP + business lifecycle of a request.
 * <p>
 * Subclasses override whichever callbacks they need. The default implementations do nothing.
 * All callbacks are invoked synchronously during {@link Http4jRequest#executeForData()}.
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
 *
 * @see Http4jConfig#setDefaultObserver(ResultObserver)
 * @see Http4jRequest#observe(ResultObserver)
 */
public class ResultObserver {

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
    }

    /**
     * Called when the HTTP response is received and the {@link ResultRule} judges it a
     * business success (e.g. JSON {@code code == 0}).
     */
    public void callBusinessSuccess() {
    }

    /**
     * Called when the HTTP response is received but the {@link ResultRule} judges it a
     * business failure.
     *
     * @param code    business error code from the response
     * @param message business error message from the response
     */
    public void callBusinessFail(int code, String message) {
    }
}
