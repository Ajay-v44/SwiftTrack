package com.swifttrack.services;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;

import com.swifttrack.FeignClients.AuthInterface;
import com.swifttrack.dto.RegisterUser;
import com.swifttrack.dto.TokenResponse;
import com.swifttrack.dto.LoginResponse;
import com.swifttrack.dto.LoginUser;
import com.swifttrack.dto.Message;
import com.swifttrack.dto.MobileNumAuth;
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
            System.err.println("[TENANT-SERVICE] Auth Service Error in register: " + errorMessage);
            throw new CustomException(HttpStatus.valueOf(e.status()), errorMessage);
        } catch (Exception e) {
            System.err.println("[TENANT-SERVICE] Unexpected Error in register: " + e.getMessage());
            e.printStackTrace();
            throw new CustomException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
    
    public LoginResponse login(LoginUser loginUser) {
        try {
            return authInterface.login(loginUser).getBody();
        } catch (FeignException e) {
            String errorMessage = extractErrorMessage(e.contentUTF8());
            System.err.println("[TENANT-SERVICE] Auth Service Error in login: " + errorMessage);
            throw new CustomException(HttpStatus.valueOf(e.status()), errorMessage);
        } catch (Exception e) {
            System.err.println("[TENANT-SERVICE] Unexpected Error in login: " + e.getMessage());
            e.printStackTrace();
            throw new CustomException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public LoginResponse loginMobileNumAndOtp(MobileNumAuth mobileNumAuth) {
        try {
            return authInterface.loginMobileNumAndOtp(mobileNumAuth).getBody();
        } catch (FeignException e) {
            String errorMessage = extractErrorMessage(e.contentUTF8());
            System.err.println("[TENANT-SERVICE] Auth Service Error in loginMobileNumAndOtp: " + errorMessage);
            throw new CustomException(HttpStatus.valueOf(e.status()), errorMessage);
        } catch (Exception e) {
            System.err.println("[TENANT-SERVICE] Unexpected Error in loginMobileNumAndOtp: " + e.getMessage());
            e.printStackTrace();
            throw new CustomException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private String extractErrorMessage(String responseBody) {
        try {
            if (responseBody == null || responseBody.isEmpty()) {
                return "No error details available";
            }
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> errorMap = mapper.readValue(responseBody, Map.class);
            Object message = errorMap.get("message");
            if (message != null && !message.toString().isEmpty()) {
                return message.toString();
            }
            Object error = errorMap.get("error");
            if (error != null) {
                return error.toString();
            }
            return responseBody;
        } catch (Exception e) {
            return responseBody != null ? responseBody : "Unknown error occurred";
        }
    }

    public TokenResponse getUserDetails(String token) {
        return authInterface.getUserDetails(token).getBody();
    }
}
