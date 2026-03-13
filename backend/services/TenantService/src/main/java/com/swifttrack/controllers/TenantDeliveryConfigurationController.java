package com.swifttrack.controllers;

import com.swifttrack.dto.AddDeliveryOptionInput;
import com.swifttrack.dto.DeliveryOptionResponse;
import com.swifttrack.dto.Message;
import com.swifttrack.dto.TenantDeliveryPriorityInput;
import com.swifttrack.dto.tenantDto.TenantDeliveryConf;
import com.swifttrack.services.TenantDeliveryConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tenant-delivery")
@Tag(name = "Tenant Delivery Configuration", description = "Manage tenant delivery options and priorities")
public class TenantDeliveryConfigurationController {

    private final TenantDeliveryConfigurationService tenantDeliveryConfigurationService;

    public TenantDeliveryConfigurationController(
            TenantDeliveryConfigurationService tenantDeliveryConfigurationService) {
        this.tenantDeliveryConfigurationService = tenantDeliveryConfigurationService;
    }

    @GetMapping("/v1/options/active")
    @Operation(summary = "Get active delivery options", description = "Returns all active delivery options")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved active delivery options")
    public ResponseEntity<List<DeliveryOptionResponse>> getActiveDeliveryOptions() {
        return ResponseEntity.ok(tenantDeliveryConfigurationService.getActiveDeliveryOptions());
    }

    @PostMapping("/v1/options")
    @Operation(summary = "Add delivery option", description = "Adds a new delivery option with default active status")
    @ApiResponse(responseCode = "200", description = "Delivery option added successfully")
    public ResponseEntity<Message> addDeliveryOption(@RequestBody AddDeliveryOptionInput input) {
        return ResponseEntity.ok(tenantDeliveryConfigurationService.addDeliveryOption(input));
    }

    @PostMapping("/v1/configure")
    @Operation(summary = "Configure tenant delivery system", description = "Configure delivery options with priority for the authenticated tenant")
    @ApiResponse(responseCode = "200", description = "Tenant delivery system configured successfully")
    public ResponseEntity<Message> configureTenantDeliverySystem(
            @RequestHeader String token,
            @RequestBody List<TenantDeliveryPriorityInput> deliveryOptionsWithPriority) {
        return ResponseEntity.ok(
                tenantDeliveryConfigurationService.configureTenantDeliverySystem(token, deliveryOptionsWithPriority));
    }

    @GetMapping("/v1/configure")
    @Operation(summary = "Get tenant delivery configuration", description = "Returns the delivery configuration for the authenticated tenant")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved tenant delivery configuration")
    public ResponseEntity<List<TenantDeliveryConf>> getTenantDeliveryConfiguration(@RequestHeader String token) {
        return ResponseEntity.ok(tenantDeliveryConfigurationService.getTenantDeliveryConfiguration(token));
    }

    @GetMapping("/v1/configure/internal")
    @Operation(summary = "Get tenant delivery configuration (internal)", description = "Internal endpoint to fetch delivery configuration by tenantId")
    public ResponseEntity<List<TenantDeliveryConf>> getTenantDeliveryConfigurationInternal(@RequestParam UUID tenantId) {
        return ResponseEntity.ok(tenantDeliveryConfigurationService.getTenantDeliveryConfigurationByTenantId(tenantId));
    }
}
