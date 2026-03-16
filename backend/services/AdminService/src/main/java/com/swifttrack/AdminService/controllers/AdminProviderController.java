package com.swifttrack.AdminService.controllers;

import com.swifttrack.AdminService.clients.ProviderClient;
import com.swifttrack.AdminService.security.AdminGuard;
import com.swifttrack.AdminService.services.AuditService;
import com.swifttrack.dto.Message;
import com.swifttrack.dto.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Section: Provider Management
 * Admin APIs to manage delivery providers (e.g. Porter, Shadowfax, Dunzo).
 */
@RestController
@RequestMapping("/api/admin/providers")
@Tag(name = "Admin - Provider Management",
        description = "Manage delivery service providers. Add, view, enable/disable providers and configure tenant-provider mappings. Admin-only.")
@RequiredArgsConstructor
public class AdminProviderController {

    private final AdminGuard adminGuard;
    private final ProviderClient providerClient;
    private final AuditService auditService;

    @GetMapping("/v1/list")
    @Operation(summary = "List all providers",
            description = "Retrieve all delivery providers registered in the system.")
    public ResponseEntity<?> listAllProviders(@RequestHeader String token) {
        adminGuard.requireAdmin(token);
        return providerClient.getAllProviders();
    }

    @GetMapping("/v1/byStatus")
    @Operation(summary = "List providers by active status",
            description = "Get all providers filtered by their active/inactive status.")
    public ResponseEntity<?> listProvidersByStatus(
            @RequestHeader String token,
            @RequestParam Boolean status) {
        adminGuard.requireAdmin(token);
        return providerClient.getProvidersByStatus(token, status);
    }

    @PostMapping("/v1/create")
    @Operation(summary = "Create a new delivery provider",
            description = "Register a new delivery provider with serviceable areas. SUPER_ADMIN only.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Provider created"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires SUPER_ADMIN")
    })
    public ResponseEntity<Message> createProvider(
            @RequestHeader String token,
            @RequestBody Object providerRequest) {
        TokenResponse admin = adminGuard.requireSuperAdmin(token);
        ResponseEntity<Message> response = providerClient.createProvider(token, providerRequest);

        auditService.log(admin, "PROVIDER_CREATE", "PROVIDER", null, "PROVIDER",
                "Admin created a new provider");

        return response;
    }

    @PutMapping("/v1/updateStatus")
    @Operation(summary = "Enable or disable a provider",
            description = "Toggle a provider's active status. Disabling stops it from receiving new orders.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Provider status updated"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires SUPER_ADMIN"),
            @ApiResponse(responseCode = "404", description = "Provider not found")
    })
    public ResponseEntity<Message> updateProviderStatus(
            @RequestHeader String token,
            @RequestParam UUID providerId,
            @RequestParam Boolean status) {
        TokenResponse admin = adminGuard.requireSuperAdmin(token);
        ResponseEntity<Message> response = providerClient.updateProviderStatus(token, providerId, status);

        auditService.log(admin, "PROVIDER_STATUS_UPDATE", "PROVIDER", providerId, "PROVIDER",
                "Provider " + providerId + " status set to: " + status);

        return response;
    }

    @PutMapping("/v1/tenant/setProviderStatus")
    @Operation(summary = "Enable or disable a tenant's provider configuration",
            description = "Enable or disable a specific provider configured for a tenant without removing the config.")
    public ResponseEntity<Message> setTenantProviderStatus(
            @RequestHeader String token,
            @RequestParam UUID providerId,
            @RequestParam Boolean enabled,
            @RequestParam(required = false) String disabledReason) {
        TokenResponse admin = adminGuard.requireAdmin(token);
        ResponseEntity<Message> response = providerClient.setTenantProviderStatus(token, providerId, enabled, disabledReason);

        auditService.log(admin, "TENANT_PROVIDER_STATUS_UPDATE", "PROVIDER", providerId, "TENANT_PROVIDER",
                "Tenant provider " + providerId + " enabled=" + enabled + ", reason=" + disabledReason);

        return response;
    }
}
