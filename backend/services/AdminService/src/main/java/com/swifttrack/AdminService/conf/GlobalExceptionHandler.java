package com.swifttrack.AdminService.conf;

import com.swifttrack.exception.CustomException;
import com.swifttrack.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

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

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, Object>> handleCustomException(CustomException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        Map<String, Object> errorResponse = createErrorResponse(
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                path
        );
        System.err.println("[ADMIN-SERVICE] Custom Exception at " + path + ": " + ex.getMessage());
        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(), path
        );
        System.err.println("[ADMIN-SERVICE] Resource Not Found at " + path + ": " + ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AdminAccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAdminAccessDeniedException(
            AdminAccessDeniedException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.FORBIDDEN.value(), "Forbidden", ex.getMessage(), path
        );
        System.err.println("[ADMIN-SERVICE] Access Denied at " + path + ": " + ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        String message = ex.getMessage() != null ? ex.getMessage() : ex.toString();
        Map<String, Object> errorResponse = createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", message, path
        );
        System.err.println("[ADMIN-SERVICE] Unexpected Error at " + path + ": " + message);
        ex.printStackTrace();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
