package com.swifttrack.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swifttrack.dto.TenantSetupStatusResponse;
import com.swifttrack.services.TenantSetupService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/tenant")
@Tag(name = "Tenant Setup", description = "Tenant onboarding and setup progress")
public class TenantSetupController {

    private final TenantSetupService tenantSetupService;

    public TenantSetupController(TenantSetupService tenantSetupService) {
        this.tenantSetupService = tenantSetupService;
    }

    @GetMapping("/v1/setup-status")
    @Operation(summary = "Get tenant setup status", description = "Returns onboarding progress for the authenticated tenant user")
    public ResponseEntity<TenantSetupStatusResponse> getSetupStatus(@RequestHeader("token") String token) {
        return ResponseEntity.ok(tenantSetupService.getSetupStatus(token));
    }
}
