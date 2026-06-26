package com.http4j;

public class Http4jException extends RuntimeException {
    public Http4jException(String message) {
        super(message);
    }

    public Http4jException(String message, Throwable cause) {
        super(message, cause);
    }

    public Http4jException(Throwable cause) {
        super(cause);
    }
}
