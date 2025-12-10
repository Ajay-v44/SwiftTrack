package com.swifttrack.ProviderService.controllers;

import com.swifttrack.dto.RegisterUser;
import com.swifttrack.dto.TokenResponse;
import com.swifttrack.dto.LoginResponse;
import com.swifttrack.dto.LoginUser;
import com.swifttrack.dto.Message;
import com.swifttrack.dto.MobileNumAuth;
import com.swifttrack.FeignClient.AuthInterface;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;


@RestController
@RequestMapping("/api/provider")
@Tag(name = "Provider Authentication", description = "Provider-level authentication and user management gateway")
public class AuthController {
    
    @Autowired
    private AuthInterface authInterface;

    @PostMapping("/v1/register")
    @Operation(summary = "Register a new provider user", description = "Create a new user account for a provider with email, mobile, and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "409", description = "Email or mobile already taken")
    })
    public ResponseEntity<String> registerUser(@RequestBody RegisterUser registerUser){
        return authInterface.registerUser(registerUser);
    }

    @PostMapping("v1/login/emailAndPassword")
    @Operation(summary = "Provider user login with email", description = "Authenticate a provider user using email and password credentials")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful, returns JWT token"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "403", description = "Account not activated")
    })
    public ResponseEntity<LoginResponse> login(@RequestBody LoginUser loginUser){
        return authInterface.login(loginUser);
    }

    @PostMapping("/v1/login/mobileAndOtp")
    @Operation(summary = "Provider user login with mobile OTP", description = "Authenticate a provider user using mobile number and one-time password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful, returns JWT token"),
            @ApiResponse(responseCode = "401", description = "Invalid OTP"),
            @ApiResponse(responseCode = "403", description = "Account not verified")
    })
    public ResponseEntity<LoginResponse> loginMobileNumAndOtp(@RequestBody MobileNumAuth mobileNumAuth){
        return authInterface.loginMobileNumAndOtp(mobileNumAuth);
    }

    @PostMapping("/v1/getUserDetails")
    @Operation(summary = "Get authenticated user details", description = "Retrieve authenticated provider user details using JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User details retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid token"),
            @ApiResponse(responseCode = "404", description = "User account not found")
    })
    public ResponseEntity<TokenResponse> getUserDetails(@RequestParam String token){
        return authInterface.getUserDetails(token);
    }
}