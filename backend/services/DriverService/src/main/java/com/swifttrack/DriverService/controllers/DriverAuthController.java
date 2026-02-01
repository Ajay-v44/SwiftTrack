package com.swifttrack.DriverService.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swifttrack.FeignClient.AuthInterface;
import com.swifttrack.dto.LoginResponse;
import com.swifttrack.dto.LoginUser;
import com.swifttrack.dto.MobileNumAuth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/driver/auth")
@Tag(name = "Driver Authentication", description = "APIs for driver authentication")
public class DriverAuthController {
    AuthInterface authInterface;

    public DriverAuthController(AuthInterface authInterface) {
        this.authInterface = authInterface;
    }

    @PostMapping("/v1/loginWithEmailAndPassword")
    @Operation(summary = "Login with email and password", description = "Login with email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "403", description = "Account not activated")
    })
    public ResponseEntity<LoginResponse> loginWithEmailAndPassword(@RequestBody LoginUser loginUser) {
        return ResponseEntity.ok(authInterface.login(loginUser).getBody());
    }

    @PostMapping("/v1/loginWithMobileNumberAndOtp")
    @Operation(summary = "Login with mobile number and OTP", description = "Login with mobile number and OTP")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "403", description = "Account not activated")
    })
    public ResponseEntity<LoginResponse> loginWithMobileNumberAndOtp(@RequestBody MobileNumAuth mobileNumAuth) {
        return ResponseEntity.ok(authInterface.loginMobileNumAndOtp(mobileNumAuth).getBody());
    }

}
