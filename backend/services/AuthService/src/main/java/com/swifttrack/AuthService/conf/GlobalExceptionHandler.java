package com.swifttrack.AuthService.conf;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.swifttrack.exception.CustomException;
import com.swifttrack.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Hidden;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Hidden
public class GlobalExceptionHandler {

    private Map<String, Object> createErrorResponse(int status, String error, String message, String path) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", System.currentTimeMillis());
        errorResponse.put("status", status);
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        errorResponse.put("path", path);
        return errorResponse;
    }

    @ExceptionHandler(JsonMappingException.class)
    public ResponseEntity<Map<String, Object>> handleJsonMappingException(
            JsonMappingException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        String message = ex.getOriginalMessage() != null ? ex.getOriginalMessage() : ex.getMessage();
        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Request Format",
                "JSON parsing error: " + message,
                path
        );
        System.err.println("[AUTH-SERVICE] JSON Parsing Error at " + path + ": " + message);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, Object>> handleCustomException(
            CustomException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        Map<String, Object> errorResponse = createErrorResponse(
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                path
        );
        System.err.println("[AUTH-SERVICE] Custom Exception at " + path + ": " + ex.getMessage());
        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                path
        );
        System.err.println("[AUTH-SERVICE] Resource Not Found at " + path + ": " + ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        String message = ex.getMessage() != null ? ex.getMessage() : ex.toString();
        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                message,
                path
        );
        System.err.println("[AUTH-SERVICE] Unexpected Error at " + path + ": " + message);
        ex.printStackTrace();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
