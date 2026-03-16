package com.swifttrack.AdminService.clients;

import com.swifttrack.dto.GetProviders;
import com.swifttrack.dto.Message;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "providerservice", url = "http://localhost:8080/providerservice")
public interface ProviderClient {

    @GetMapping("/api/providers/v1/list")
    ResponseEntity<List<GetProviders>> getAllProviders();

    @GetMapping("/api/providers/v1/getProviderByStatus")
    ResponseEntity<List<GetProviders>> getProvidersByStatus(
            @RequestHeader String token,
            @RequestParam Boolean status);

    @PutMapping("/api/providers/v1/updateProviderStatus")
    ResponseEntity<Message> updateProviderStatus(
            @RequestHeader String token,
            @RequestParam UUID providerId,
            @RequestParam Boolean status);

    @PostMapping("/api/providers/v1/create")
    ResponseEntity<Message> createProvider(
            @RequestHeader String token,
            @RequestBody Object provider);

    @PutMapping("/api/providers/v1/tenantProviders/status")
    ResponseEntity<Message> setTenantProviderStatus(
            @RequestHeader String token,
            @RequestParam UUID providerId,
            @RequestParam Boolean enabled,
            @RequestParam(required = false) String disabledReason);
}
