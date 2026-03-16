package com.swifttrack.AdminService.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "aidispatchservice", url = "http://localhost:8080/aidispatchservice")
public interface AIDispatchClient {

    @PostMapping("/dispatch/assign")
    ResponseEntity<?> assignDriver(@RequestBody Object request);

    @GetMapping("/dispatch/health")
    ResponseEntity<String> health();
}
