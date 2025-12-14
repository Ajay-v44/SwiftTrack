package com.swifttrack.map.exception;

/**
 * Exception thrown when geocoding fails
 */
public class GeocodingException extends MapServiceException {
    
    public GeocodingException(String message) {
        super("GEOCODING_ERROR", message);
    }
    
    public GeocodingException(String message, Throwable cause) {
        super("GEOCODING_ERROR", message, cause);
    }
}
