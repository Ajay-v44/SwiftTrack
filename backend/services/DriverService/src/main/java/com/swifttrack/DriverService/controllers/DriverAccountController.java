package com.swifttrack.DriverService.controllers;

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

}
