package com.swifttrack.services;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;

import com.swifttrack.FeignClients.AuthInterface;
import com.swifttrack.RegisterUser;
import com.swifttrack.Message;
import com.swifttrack.exception.CustomException;

import java.util.Map;

@Service
public class AuthService {

    AuthInterface authInterface;

    public AuthService(AuthInterface authInterface) {
        this.authInterface = authInterface;
    }

    public Message register(RegisterUser registerUser) {
        try {
            System.out.println("Registering user: " + registerUser);
            return new Message(authInterface.registerUser(registerUser).getBody());
        } catch (FeignException e) {
            String errorMessage = extractErrorMessage(e.contentUTF8());
            throw new CustomException(HttpStatus.valueOf(e.status()), errorMessage);
        } catch (Exception e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private String extractErrorMessage(String responseBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> errorMap = mapper.readValue(responseBody, Map.class);
            Object message = errorMap.get("message");
            return message != null ? message.toString() : "An error occurred";
        } catch (Exception e) {
            return responseBody != null ? responseBody : "An error occurred";
        }
    }
}
