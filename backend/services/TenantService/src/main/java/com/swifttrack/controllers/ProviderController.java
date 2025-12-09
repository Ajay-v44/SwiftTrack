package com.swifttrack.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swifttrack.dto.GetProviders;
import com.swifttrack.services.ProviderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/providers")
@Tag(name = "Provider Controller", description = "Provides a list of providers")
public class ProviderController {

    private ProviderService providerService;

    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @GetMapping("/v1/getProviders")
    @Operation(summary = "Get providers", description = "Returns a list of providers")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of providers")
    public ResponseEntity<List<GetProviders>> getProviders() {
        return ResponseEntity.ok(providerService.getProviders());

    }
}
