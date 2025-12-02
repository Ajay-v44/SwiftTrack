package com.swifttrack.AuthService.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swifttrack.AuthService.Dto.LoginResponse;
import com.swifttrack.AuthService.Dto.LoginUser;
import com.swifttrack.AuthService.Dto.MobileNumAuth;
import com.swifttrack.AuthService.Dto.RegisterUser;
import com.swifttrack.AuthService.Dto.TokenResponse;
import com.swifttrack.AuthService.Services.UserServices;

@RestController
@RequestMapping("/api/users/")
public class UserController {

    @Autowired
    private UserServices userService;

    @PostMapping("v1/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterUser input) {
        return ResponseEntity.ok(userService.registerUser(input));
    }

    @PostMapping("v1/login/emailAndPassword")
    public ResponseEntity<LoginResponse> loginUserEmailAndPassword(@RequestBody LoginUser input) {
        return ResponseEntity.ok(userService.loginUserEmailAndPassword(input));
    }

    @PostMapping("v1/login/mobileNumAndOtp")
    public ResponseEntity<LoginResponse> loginUserMobileAndOtp(@RequestBody MobileNumAuth entity) {
        return ResponseEntity.ok(userService.loginMobileAndOtp(entity));
    }

    @PostMapping("v1/getUserDetails")
    public ResponseEntity<TokenResponse> getUserDetails(@RequestParam String token) {
        return ResponseEntity.ok(userService.getUserDetails(token));
    }

}