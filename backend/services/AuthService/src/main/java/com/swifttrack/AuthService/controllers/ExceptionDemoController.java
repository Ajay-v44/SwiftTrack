package com.swifttrack.AuthService.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swifttrack.exception.CustomException;
import com.swifttrack.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/api/demo/exceptions")
public class ExceptionDemoController {

    /**
     * Endpoint to demonstrate different exception types and their HTTP status codes
     * 
     * @param type The type of exception to throw (notfound, unauthorized, forbidden, conflict, badrequest, internalerror)
     */
    @GetMapping("/throw")
    public void throwException(@RequestParam String type) {
        switch (type.toLowerCase()) {
            case "notfound":
                // This will result in a 404 Not Found response
                throw new ResourceNotFoundException("Resource not found");
                
            case "unauthorized":
                // This will result in a 401 Unauthorized response
                throw new CustomException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
                
            case "forbidden":
                // This will result in a 403 Forbidden response
                throw new CustomException(HttpStatus.FORBIDDEN, "Access forbidden");
                
            case "conflict":
                // This will result in a 409 Conflict response
                throw new CustomException(HttpStatus.CONFLICT, "Resource conflict");
                
            case "badrequest":
                // This will result in a 400 Bad Request response
                throw new CustomException(HttpStatus.BAD_REQUEST, "Bad request");
                
            case "internalerror":
                // This will result in a 500 Internal Server Error response
                throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
                
            default:
                // This will result in a 400 Bad Request response
                throw new CustomException(HttpStatus.BAD_REQUEST, "Invalid scenario provided");
        }
    }
}