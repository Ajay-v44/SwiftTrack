package com.swifttrack.AdminService.controllers;

import com.swifttrack.AdminService.clients.DriverClient;
import com.swifttrack.AdminService.security.AdminGuard;
import com.swifttrack.AdminService.services.AuditService;
import com.swifttrack.dto.Message;
import com.swifttrack.dto.TokenResponse;
import com.swifttrack.enums.VerificationStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Section: Driver Management
 * Admin APIs to list, approve/reject, monitor, and manage driver assignments.
 */
@RestController
@RequestMapping("/api/admin/drivers")
@Tag(name = "Admin - Driver Management",
        description = "Driver verification, approval, live status monitoring, and assignment management. Admin-only.")
@RequiredArgsConstructor
public class AdminDriverController {

    private final AdminGuard adminGuard;
    private final DriverClient driverClient;
    private final AuditService auditService;

    @GetMapping("/v1/list")
    @Operation(summary = "List all drivers by verification status",
            description = "Get all platform drivers filtered by their verification status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Drivers listed"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<?> listDrivers(
            @RequestHeader String token,
            @RequestParam VerificationStatus verificationStatus) {
        adminGuard.requireAdmin(token);
        return driverClient.getDriverUsers(token, verificationStatus);
    }

    @GetMapping("/v1/tenant-drivers")
    @Operation(summary = "List all tenant drivers",
            description = "Get all drivers associated with the admin's tenant.")
    public ResponseEntity<?> listTenantDrivers(@RequestHeader String token) {
        adminGuard.requireAdmin(token);
        return driverClient.getTenantDrivers(token);
    }

    @PostMapping("/v1/approve/{driverId}")
    @Operation(summary = "Approve a driver",
            description = "Approve a driver application and create their billing account. " +
                    "This activates the driver on the platform.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Driver approved and billing account created"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    public ResponseEntity<Message> approveDriver(
            @RequestHeader String token,
            @PathVariable UUID driverId) {
        TokenResponse admin = adminGuard.requireAdmin(token);
        ResponseEntity<Message> response = driverClient.acceptDriver(token, driverId);

        auditService.log(admin, "DRIVER_APPROVE", "DRIVER", driverId, "DRIVER",
                "Admin approved driver: " + driverId);

        return response;
    }

    @GetMapping("/v1/status/{driverId}")
    @Operation(summary = "Get driver live status",
            description = "Retrieve the current online/offline status of a specific driver.")
    public ResponseEntity<?> getDriverStatus(
            @RequestHeader String token,
            @PathVariable UUID driverId) {
        adminGuard.requireAdmin(token);
        return driverClient.getDriverStatus(driverId);
    }

    @GetMapping("/v1/location/{driverId}")
    @Operation(summary = "Get driver live location",
            description = "Retrieve the current GPS coordinates of a specific driver.")
    public ResponseEntity<?> getDriverLocation(
            @RequestHeader String token,
            @PathVariable UUID driverId) {
        adminGuard.requireAdmin(token);
        return driverClient.getDriverLocation(driverId);
    }

    @GetMapping("/v1/available/{driverId}")
    @Operation(summary = "Check driver availability",
            description = "Check if a driver is currently available (ONLINE and not on assignment).")
    public ResponseEntity<Boolean> isDriverAvailable(
            @RequestHeader String token,
            @PathVariable UUID driverId) {
        adminGuard.requireAdmin(token);
        return driverClient.isDriverAvailable(driverId);
    }

    @PostMapping("/v1/cancelAssignment")
    @Operation(summary = "Cancel a driver's assigned order",
            description = "Force-cancel an order assignment for a driver. Use with caution.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assignment cancelled"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Message> cancelDriverAssignment(
            @RequestHeader String token,
            @RequestParam UUID orderId,
            @RequestParam(required = false) String reason) {
        TokenResponse admin = adminGuard.requireAdmin(token);
        ResponseEntity<Message> response = driverClient.cancelAssignedOrderInternal(orderId, reason);

        auditService.log(admin, "DRIVER_ASSIGNMENT_CANCEL", "DRIVER", orderId, "ORDER",
                "Force-cancelled assignment for order: " + orderId + ", reason: " + reason);

        return response;
    }
}
