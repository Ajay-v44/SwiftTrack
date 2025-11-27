package com.swifttrack.AuthService.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swifttrack.AuthService.Dto.LoginResponse;
import com.swifttrack.AuthService.Dto.LoginUser;
import com.swifttrack.AuthService.Dto.RegisterUser;
import com.swifttrack.AuthService.Services.UserServices;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserServices userService;

    @PostMapping("v1/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterUser input) {
        try {
            return ResponseEntity.ok(userService.registerUser(input));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("v1/login/emailAndPassword")
    public ResponseEntity<LoginResponse> loginUserEmailAndPassword(@RequestBody LoginUser input) {
        try {
            return ResponseEntity.ok(userService.loginUserEmailAndPassword(input));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new LoginResponse(e.getMessage(), null));
        }
    }

}