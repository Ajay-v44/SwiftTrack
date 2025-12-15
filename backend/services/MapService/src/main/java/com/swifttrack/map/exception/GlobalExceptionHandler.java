package com.swifttrack.map.exception;

import com.swifttrack.map.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.stream.Collectors;

/**
 * Global exception handler for Map Service
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(GeocodingException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeocodingException(GeocodingException ex) {
        log.warn("Geocoding error: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage()));
    }
    
    @ExceptionHandler(RoutingException.class)
    public ResponseEntity<ApiResponse<Void>> handleRoutingException(RoutingException ex) {
        log.warn("Routing error: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage()));
    }
    
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleRateLimitException(RateLimitExceededException ex) {
        log.warn("Rate limit exceeded: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.TOO_MANY_REQUESTS)
            .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage()));
    }
    
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiResponse<Void>> handleExternalServiceException(ExternalServiceException ex) {
        log.error("External service error: {}", ex.getMessage(), ex);
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage()));
    }
    
    @ExceptionHandler(MapServiceException.class)
    public ResponseEntity<ApiResponse<Void>> handleMapServiceException(MapServiceException ex) {
        log.error("Map service error: {}", ex.getMessage(), ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        
        log.warn("Validation error: {}", errors);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("VALIDATION_ERROR", "Validation failed", errors));
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException ex) {
        String errors = ex.getConstraintViolations().stream()
            .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
            .collect(Collectors.joining(", "));
        
        log.warn("Constraint violation: {}", errors);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("VALIDATION_ERROR", "Validation failed", errors));
    }
    
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameterException(MissingServletRequestParameterException ex) {
        log.warn("Missing parameter: {}", ex.getParameterName());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("MISSING_PARAMETER", 
                "Required parameter '" + ex.getParameterName() + "' is missing"));
    }
    
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ApiResponse<Void>> handleWebClientResponseException(WebClientResponseException ex) {
        log.error("External API error: {} - {}", ex.getStatusCode(), ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ApiResponse.error("EXTERNAL_API_ERROR", 
                "External API error: " + ex.getMessage()));
    }
    
    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleWebClientRequestException(WebClientRequestException ex) {
        log.error("External API connection error: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ApiResponse.error("CONNECTION_ERROR", 
                "Failed to connect to external service"));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}
