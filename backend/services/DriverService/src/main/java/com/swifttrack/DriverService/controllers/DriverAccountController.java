package com.swifttrack.DriverService.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swifttrack.DriverService.services.DriverService;
import com.swifttrack.dto.Message;
import com.swifttrack.dto.driverDto.AddTenantDriver;
import com.swifttrack.dto.driverDto.GetDriverUserDetails;
import com.swifttrack.dto.driverDto.GetTenantDrivers;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/driver")
@Tag(name = "Driver Account", description = "Driver account management and authentication gateway service for SwiftTrack platform.")
public class DriverAccountController {
    DriverService driverService;

    public DriverAccountController(DriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping("/v1/getDriverDetails")
    @Operation(summary = "Get authenticated driver details", description = "Retrieve authenticated driver details using JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Driver details retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid token"),
            @ApiResponse(responseCode = "404", description = "Driver account not found")
    })
    public ResponseEntity<GetDriverUserDetails> getDriverDetails(@RequestHeader String token) {
        return ResponseEntity.ok(driverService.getDriverUserDetails(token));
    }

    @PostMapping("/v1/addTenantDrivers")
    @Operation(summary = "Add tenant drivers", description = "Add drivers to a tenant account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Drivers added successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "403", description = "Account not activated")
    })
    public ResponseEntity<Message> addTenantDrivers(@RequestHeader String token,
            @RequestBody AddTenantDriver entity) {
        return ResponseEntity.ok(driverService.createDriverProfile(token, entity));
    }

    @PostMapping("/v1/updateStatus")
    @Operation(summary = "Update Driver Status", description = "Update the online/offline status of the driver")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid token/Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<Message> updateStatus(@RequestHeader String token,
            @RequestBody com.swifttrack.dto.driverDto.UpdateDriverStatusRequest request) {
        return ResponseEntity.ok(driverService.updateDriverStatus(token, request));
    }

    @GetMapping("/v1/status/{driverId}")
    @Operation(summary = "Get Driver Status (Internal)", description = "Get current status of a driver by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Driver status retrieved"),
            @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    public ResponseEntity<com.swifttrack.DriverService.models.DriverStatus> getDriverStatus(
            @org.springframework.web.bind.annotation.PathVariable java.util.UUID driverId) {
        return ResponseEntity.ok(driverService.getDriverStatus(driverId));
    }

    @GetMapping("/v1/location/{driverId}")
    @Operation(summary = "Get Driver Location (Internal)", description = "Get current live location of a driver by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Driver location retrieved"),
            @ApiResponse(responseCode = "404", description = "Driver location not found")
    })
    public ResponseEntity<com.swifttrack.DriverService.models.DriverLocationLive> getDriverLocation(
            @org.springframework.web.bind.annotation.PathVariable java.util.UUID driverId) {
        return ResponseEntity.ok(driverService.getDriverLocation(driverId));
    }

    @GetMapping("/v1/available/{driverId}")
    @Operation(summary = "Check Driver Availability (Internal)", description = "Check if driver is currently available (ONLINE)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Availability check successful")
    })
    public ResponseEntity<Boolean> isDriverAvailable(
            @org.springframework.web.bind.annotation.PathVariable java.util.UUID driverId) {
        return ResponseEntity.ok(driverService.isDriverAvailable(driverId));
    }

    @PostMapping("/v1/location")
    @Operation(summary = "Update Driver Location", description = "Update the live location of the authenticated driver")
    public ResponseEntity<Message> updateLocation(@RequestHeader String token,
            @RequestBody com.swifttrack.dto.driverDto.DriverLocationUpdateDto request) {
        com.swifttrack.dto.TokenResponse userDetails = driverService.validateToken(token);
        driverService.updateDriverLocation(userDetails.id(), request.latitude(), request.longitude());
        return ResponseEntity.ok(new Message("Location updated successfully"));
    }

    @PostMapping("/v1/assign-order")
    @Operation(summary = "Assign Order", description = "Assign an order to a specific driver")
    public ResponseEntity<com.swifttrack.DriverService.models.DriverOrderAssignment> assignOrder(
            @RequestBody com.swifttrack.dto.driverDto.AssignOrderRequest request) {
        return ResponseEntity.ok(driverService.assignOrder(request.driverId(), request.orderId()));
    }

    @PostMapping("/v1/respond-assignment")
    @Operation(summary = "Respond to Assignment", description = "Driver accepts or rejects an order assignment")
    public ResponseEntity<Message> respondToAssignment(@RequestHeader String token,
            @RequestBody com.swifttrack.dto.driverDto.RespondToAssignmentDto request) {
        // Validation could be added here to ensure the assignment belongs to the driver
        driverService.respondToAssignment(request.assignmentId(), request.accept(), request.reason());
        return ResponseEntity.ok(new Message("Response recorded successfully"));
    }

    @PostMapping("/v1/getTenantDrivers")
    @Operation(summary = "Get Tenant Drivers", description = "Get all drivers of a tenant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Drivers retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid token"),
            @ApiResponse(responseCode = "404", description = "Tenant not found")
    })
    public ResponseEntity<List<GetTenantDrivers>> getTenantDrivers(@RequestHeader String token) {
        return ResponseEntity.ok(driverService.getTenantDrivers(token));
    }

}
