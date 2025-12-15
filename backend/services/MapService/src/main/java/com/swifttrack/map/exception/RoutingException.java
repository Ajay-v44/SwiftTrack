package com.swifttrack.map.exception;

/**
 * Exception thrown when routing fails
 */
public class RoutingException extends MapServiceException {
    
    public RoutingException(String message) {
        super("ROUTING_ERROR", message);
    }
    
    public RoutingException(String message, Throwable cause) {
        super("ROUTING_ERROR", message, cause);
    }
}
