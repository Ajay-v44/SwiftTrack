package com.swifttrack.DriverService.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/driver")
@Tag(name = "Driver Account", description = "Driver account management and authentication gateway service for SwiftTrack platform.")
public class DriverAccountController {

    @GetMapping("/v1/getDriverDetails")
    @Operation(summary = "Get authenticated driver details", description = "Retrieve authenticated driver details using JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Driver details retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid token"),
            @ApiResponse(responseCode = "404", description = "Driver account not found")
    })
    public ResponseEntity<?> getDriverDetails(@RequestHeader String token) {
        return ResponseEntity.ok("Driver Details");
    }

}
