package com.swifttrack.services;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;

import com.swifttrack.FeignClients.AuthInterface;
import com.swifttrack.dto.RegisterUser;
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
            throw new CustomException(HttpStatus.valueOf(e.status()), e.getMessage());
        } catch (Exception e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
    
    public LoginResponse login(LoginUser loginUser) {
        try {
            return authInterface.login(loginUser).getBody();
        } catch (FeignException e) {
            throw new CustomException(HttpStatus.valueOf(e.status()), e.getMessage());
        } catch (Exception e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public LoginResponse loginMobileNumAndOtp(MobileNumAuth mobileNumAuth) {
        try {
            return authInterface.loginMobileNumAndOtp(mobileNumAuth).getBody();
        } catch (FeignException e) {
            throw new CustomException(HttpStatus.valueOf(e.status()), e.getMessage());
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
