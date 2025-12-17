package com.swifttrack.FeignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.swifttrack.dto.TokenResponse;

@FeignClient(name = "authservice")
public interface AuthInterface {
    @PostMapping("/api/users/v1/getUserDetails")
    public ResponseEntity<TokenResponse> getUserDetails(@RequestParam String token);
}
