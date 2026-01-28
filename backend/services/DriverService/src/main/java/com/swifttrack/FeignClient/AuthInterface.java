package com.swifttrack.FeignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.swifttrack.dto.LoginResponse;
import com.swifttrack.dto.LoginUser;
import com.swifttrack.dto.MobileNumAuth;
import com.swifttrack.dto.TokenResponse;

@FeignClient(name = "authservice", url = "http://localhost:8080/authservice")
public interface AuthInterface {

    @PostMapping("/api/users/v1/login/emailAndPassword")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginUser loginUser);

    @PostMapping("/api/users/v1/login/mobileNumAndOtp")
    public ResponseEntity<LoginResponse> loginMobileNumAndOtp(@RequestBody MobileNumAuth mobileNumAuth);

    @PostMapping("/api/users/v1/getUserDetails")
    public ResponseEntity<TokenResponse> getUserDetails(@RequestHeader String token);
}
