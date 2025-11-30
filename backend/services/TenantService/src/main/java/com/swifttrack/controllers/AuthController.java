package com.swifttrack.controllers;

import com.swifttrack.RegisterUser;
import com.swifttrack.services.AuthService;
import com.swifttrack.Message;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;


@RestController
@RequestMapping("/api/tenant")
public class AuthController {
    @Autowired
    private AuthService authService;
    @PostMapping("/v1/register")
    public ResponseEntity<Message> registerUser(RegisterUser registerUser){
        return ResponseEntity.ok(authService.register(registerUser));
    }

}   
