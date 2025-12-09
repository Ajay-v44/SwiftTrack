package com.swifttrack.FeignClients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.swifttrack.dto.GetProviders;

@FeignClient(name = "providerservice")
public interface ProviderInterface {
    @GetMapping("/api/providers/v1/list")
    List<GetProviders> getProviders();
}
