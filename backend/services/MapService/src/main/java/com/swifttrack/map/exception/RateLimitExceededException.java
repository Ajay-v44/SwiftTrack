package com.swifttrack.map.exception;

/**
 * Exception thrown when rate limit is exceeded
 */
public class RateLimitExceededException extends MapServiceException {
    
    public RateLimitExceededException(String message) {
        super("RATE_LIMIT_EXCEEDED", message);
    }
    
    public RateLimitExceededException() {
        super("RATE_LIMIT_EXCEEDED", "API rate limit exceeded. Please try again later.");
    }
}
