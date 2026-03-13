
package com.swifttrack.ProviderService.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import com.swifttrack.ProviderService.dto.CreateProviderAndServicableAreas;
import com.swifttrack.ProviderService.dto.GetProviders;
import com.swifttrack.ProviderService.dto.ProviderOnBoardingInput;
import com.swifttrack.ProviderService.services.ProviderService;
import com.swifttrack.dto.Message;

@RestController
@RequestMapping("/api/providers")
@Tag(name = "Providers", description = "Provider management endpoints")
public class ProviderController {

    @Autowired
    private ProviderService providerService;

    @GetMapping("/v1/list")
    @Operation(summary = "Get all providers", description = "Retrieve list of all available providers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Providers retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal Server error")
    })
    public ResponseEntity<List<GetProviders>> getProviders() {
        System.out.println("Getting providers");
        return ResponseEntity.ok(providerService.getProviders());
    }

    @GetMapping("/v1/getProviderByStatus")
    @Operation(summary = "Get providers by status", description = "Retrieve list of providers with the given status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Providers retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal Server error")
    })
    public ResponseEntity<List<GetProviders>> getProvidersByStatus(@RequestHeader String token,
            @RequestParam Boolean status) {
        return ResponseEntity.ok(providerService.getProviderByStatus(token, status));
    }

    @PutMapping("/v1/updateProviderStatus")
    @Operation(summary = "Update provider status", description = "Enable or disable provider in provider table")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Provider status updated successfully"),
            @ApiResponse(responseCode = "403", description = "Only SUPER_ADMIN or SYSTEM_USER allowed"),
            @ApiResponse(responseCode = "404", description = "Provider not found")
    })
    public ResponseEntity<Message> updateProviderStatus(@RequestHeader String token,
            @RequestParam UUID providerId,
            @RequestParam Boolean status) {
        return ResponseEntity.ok(providerService.updateProviderStatus(token, providerId, status));
    }

    @PostMapping("/v1/create")
    @Operation(summary = "Create a new provider", description = "Create a new provider with the given details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Provider created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal Server error")
    })
    public ResponseEntity<Message> createProvider(@RequestHeader String token,
            @RequestBody CreateProviderAndServicableAreas provider) {
        return ResponseEntity.ok(providerService.createProvider(token, provider));
    }

    @PostMapping("/v1/configureTenantProviders")
    @Operation(summary = "Configure tenant providers", description = "Configure providers for a specific tenant")
    @ApiResponse(responseCode = "200", description = "Providers configured successfully")
    public ResponseEntity<Message> configureTenantProviders(@RequestHeader String token,
            @RequestBody List<UUID> providers) {
        return ResponseEntity.ok(providerService.configureTenantProviders(token, providers));
    }

    @GetMapping("/v1/getTenantProviders")
    @Operation(summary = "Get tenant providers", description = "Retrieve list of providers for a specific tenant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Providers retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal Server error")
    })
    public ResponseEntity<List<GetProviders>> getTenantProviders(@RequestHeader String token) {
        return ResponseEntity.ok(providerService.getTenantProviders(token));
    }

    @GetMapping("/v1/internal/getTenantProviders")
    public ResponseEntity<List<GetProviders>> getTenantProvidersInternal(@RequestParam UUID tenantId) {
        return ResponseEntity.ok(providerService.getTenantProvidersByTenantId(tenantId));
    }

    @PutMapping("/v1/tenantProviders/status")
    @Operation(summary = "Enable or disable tenant provider", description = "Enable or disable a provider configured for tenant")
    @ApiResponse(responseCode = "200", description = "Tenant provider status updated successfully")
    public ResponseEntity<Message> setTenantProviderStatus(@RequestHeader String token,
            @RequestParam UUID providerId,
            @RequestParam Boolean enabled,
            @RequestParam(required = false) String disabledReason) {
        return ResponseEntity.ok(providerService.setTenantProviderStatus(token, providerId, enabled, disabledReason));
    }

    @DeleteMapping("/v1/tenantProviders")
    @Operation(summary = "Remove tenant provider", description = "Remove a provider configuration for tenant")
    @ApiResponse(responseCode = "200", description = "Tenant provider removed successfully")
    public ResponseEntity<Message> removeTenantProvider(@RequestHeader String token,
            @RequestParam UUID providerId) {
        return ResponseEntity.ok(providerService.removeTenantProvider(token, providerId));
    }

    @PostMapping("/v1/requestProviderOnboarding")
    @Operation(summary = "Request provider onboarding", description = "Request provider onboarding with the given details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Provider onboarding request created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal Server error")
    })
    public ResponseEntity<Message> requestProviderOnboarding(@RequestHeader String token,
            @RequestBody ProviderOnBoardingInput providerOnboardingRequest) {
        return ResponseEntity.ok(providerService.requestProviderOnboarding(token, providerOnboardingRequest));
    }
}
