package com.swifttrack.FeignClients;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.swifttrack.dto.GetProviders;

@FeignClient(name = "providerservice")
public interface ProviderInterface {
    @GetMapping("/api/providers/v1/list")
    List<GetProviders> getProviders();

    @GetMapping("/api/providers/v1/internal/getTenantProviders")
    List<GetProviders> getTenantProviders(@RequestParam("tenantId") UUID tenantId);
}
