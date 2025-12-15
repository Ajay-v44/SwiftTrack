package com.swifttrack.map.exception;

/**
 * Base exception for Map Service errors
 */
public class MapServiceException extends RuntimeException {
    
    private final String errorCode;
    
    public MapServiceException(String message) {
        super(message);
        this.errorCode = "MAP_ERROR";
    }
    
    public MapServiceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public MapServiceException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
