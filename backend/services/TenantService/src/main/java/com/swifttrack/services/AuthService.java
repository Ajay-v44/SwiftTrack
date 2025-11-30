package com.swifttrack.services;

import org.springframework.stereotype.Service;

import com.swifttrack.FeignClients.AuthInterface;
import com.swifttrack.RegisterUser;
import com.swifttrack.Message;

@Service
public class AuthService {

    AuthInterface authInterface;

    public AuthService(AuthInterface authInterface) {
        this.authInterface = authInterface;
    }

    public Message register(RegisterUser registerUser) {
        return new Message(authInterface.registerUser(registerUser).getBody());
    }
}
