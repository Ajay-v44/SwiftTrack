package com.swifttrack.FeignClients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.swifttrack.RegisterUser;

@FeignClient(name = "auth-service")
public interface AuthInterface {

    @PostMapping("/api/users/v1/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterUser registerUser);
}
