package io.bookwright.api;

/** Transport-level failure while executing an API call. */
public class ApiCallException extends RuntimeException {

    public ApiCallException(String message, Throwable cause) {
        super(message, cause);
    }
}
