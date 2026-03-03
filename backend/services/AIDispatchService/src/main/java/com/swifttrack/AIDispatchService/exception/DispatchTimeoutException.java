package com.swifttrack.AIDispatchService.exception;

/**
 * Thrown when the dispatch pipeline exceeds the configured timeout budget.
 */
public class DispatchTimeoutException extends RuntimeException {

    public DispatchTimeoutException(String message) {
        super(message);
    }

    public DispatchTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
