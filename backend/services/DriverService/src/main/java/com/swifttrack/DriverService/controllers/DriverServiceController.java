package com.swifttrack.DriverService.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swifttrack.DriverService.dto.spatial.FindNearestDriversRequest;
import com.swifttrack.DriverService.dto.spatial.FindNearestDriversResponse;
import com.swifttrack.DriverService.services.DriverLocationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/drivers")
@Tag(name = "Driver Spatial Index", description = "Driver location updates and nearest driver retrieval")
public class DriverServiceController {

    private static final int DEFAULT_K = 15;

    private final DriverLocationService driverLocationService;

    public DriverServiceController(DriverLocationService driverLocationService) {
        this.driverLocationService = driverLocationService;
    }

    @PostMapping("/assign-nearest")
    @Operation(summary = "Assign nearest driver", description = "Find nearest and assign via AI dispatch + order assignment")
    public ResponseEntity<FindNearestDriversResponse> assignNearestDrivers(
            @RequestHeader("token") String token,
            @Valid @RequestBody FindNearestDriversRequest request) {

        List<String> nearestDrivers = driverLocationService.findNearestDrivers(
                request.pickupLat(),
                request.pickupLon(),
                DEFAULT_K);
        System.out.println("Nearest drivers: " + nearestDrivers);
        // AI dispatch runs in batches of 5 over top 15 nearest drivers.
        driverLocationService.dispatchNearestDrivers(nearestDrivers, request.orderId(), token);

        return ResponseEntity.ok(new FindNearestDriversResponse(nearestDrivers));
    }
}
