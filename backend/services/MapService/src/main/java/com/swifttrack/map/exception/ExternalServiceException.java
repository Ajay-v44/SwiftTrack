package com.swifttrack.map.exception;

/**
 * Exception thrown when external service is unavailable
 */
public class ExternalServiceException extends MapServiceException {
    
    private final String serviceName;
    
    public ExternalServiceException(String serviceName, String message) {
        super("EXTERNAL_SERVICE_ERROR", serviceName + ": " + message);
        this.serviceName = serviceName;
    }
    
    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super("EXTERNAL_SERVICE_ERROR", serviceName + ": " + message, cause);
        this.serviceName = serviceName;
    }
    
    public String getServiceName() {
        return serviceName;
    }
}
