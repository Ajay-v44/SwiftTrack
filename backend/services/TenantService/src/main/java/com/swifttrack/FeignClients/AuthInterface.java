package com.swifttrack.FeignClients;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.swifttrack.dto.LoginResponse;
import com.swifttrack.dto.LoginUser;
import com.swifttrack.dto.Message;
import com.swifttrack.dto.MobileNumAuth;
import com.swifttrack.dto.RegisterUser;
import com.swifttrack.dto.TokenResponse;

@FeignClient(name = "authservice")
public interface AuthInterface {

    @PostMapping("/api/users/v1/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterUser registerUser);

    @PostMapping("/api/users/v1/login/emailAndPassword")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginUser loginUser);

    @PostMapping("/api/users/v1/login/mobileNumAndOtp")
    public ResponseEntity<LoginResponse> loginMobileNumAndOtp(@RequestBody MobileNumAuth mobileNumAuth);

    @PostMapping("/api/users/v1/getUserDetails")
    public ResponseEntity<TokenResponse> getUserDetails(@RequestParam String token);

    @PostMapping("/api/users/v1/assignAdmin")
    public ResponseEntity<Message> assignAdmin(@RequestParam String token, @RequestParam UUID tenantId);
}
