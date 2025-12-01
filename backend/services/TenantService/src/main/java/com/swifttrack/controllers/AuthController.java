package com.swifttrack.controllers;

import com.swifttrack.dto.RegisterUser;
import com.swifttrack.services.AuthService;
import com.swifttrack.dto.LoginResponse;
import com.swifttrack.dto.LoginUser;
import com.swifttrack.dto.Message;
import com.swifttrack.dto.MobileNumAuth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;


@RestController
@RequestMapping("/api/tenant")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/v1/register")
    public ResponseEntity<Message> registerUser(@RequestBody RegisterUser registerUser){
        return ResponseEntity.ok(authService.register(registerUser));
    }

    @PostMapping("/v1/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginUser loginUser){
        return ResponseEntity.ok(authService.login(loginUser));
    }
    @PostMapping("/v1/loginMobileNumAndOtp")
    public ResponseEntity<LoginResponse> loginMobileNumAndOtp(@RequestBody MobileNumAuth mobileNumAuth){
        return ResponseEntity.ok(authService.loginMobileNumAndOtp(mobileNumAuth));
    }
}   
