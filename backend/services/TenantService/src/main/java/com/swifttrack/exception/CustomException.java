package com.swifttrack.exception;

import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException {
    private final HttpStatus status;
    
    public CustomException(HttpStatus status) {
        super(status.getReasonPhrase());
        this.status = status;
    }
    
    public CustomException(HttpStatus status, String message) {
        super(message != null ? message : status.getReasonPhrase());
        this.status = status;
    }
    
    public CustomException(HttpStatus status, String message, Throwable cause) {
        super(message != null ? message : status.getReasonPhrase(), cause);
        this.status = status;
    }
    
    public HttpStatus getStatus() {
        return status;
    }
}
