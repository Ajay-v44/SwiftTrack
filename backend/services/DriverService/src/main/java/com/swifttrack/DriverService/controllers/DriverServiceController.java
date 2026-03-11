package com.swifttrack.DriverService.controllers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swifttrack.DriverService.dto.spatial.FindNearestDriversRequest;
import com.swifttrack.DriverService.dto.spatial.FindNearestDriversResponse;
import com.swifttrack.DriverService.models.DriverOrderAssignment;
import com.swifttrack.DriverService.enums.DriverType;
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
    @Operation(summary = "Assign nearest driver", description = "Find nearest drivers scoped by driver type (TENANT_DRIVER / PLATFORM_DRIVER) and assign via AI dispatch + order assignment")
    public ResponseEntity<FindNearestDriversResponse> assignNearestDrivers(
            @RequestHeader("token") String token,
            @Valid @RequestBody FindNearestDriversRequest request) {

        String tenantId = request.tenantId() != null ? request.tenantId().toString() : null;
        DriverType driverType = request.driverType();

        // Validate: TENANT_DRIVER requests must include a tenantId
        if (driverType == DriverType.TENANT_DRIVER && tenantId == null) {
            return ResponseEntity.badRequest().build();
        }

        List<String> nearestDrivers = driverLocationService.findNearestDrivers(
                request.pickupLat(),
                request.pickupLon(),
                DEFAULT_K,
                tenantId,
                driverType);
        nearestDrivers = prioritizeExcludedDriverLast(nearestDrivers, request.excludedDriverId());

        System.out.println("Nearest drivers (" + driverType + "): " + nearestDrivers);

        // AI dispatch runs in batches of 5 over top 15 nearest drivers.
        Optional<DriverOrderAssignment> assignment = driverLocationService.dispatchNearestDrivers(nearestDrivers,
                request.orderId(), token);

        return ResponseEntity.ok(new FindNearestDriversResponse(
                nearestDrivers,
                assignment.isPresent(),
                assignment.map(DriverOrderAssignment::getDriverId).orElse(null)));
    }

    @PostMapping("/assign-nearest/internal")
    @Operation(summary = "Assign nearest driver (internal)", description = "Internal async assignment endpoint for OrderService")
    public ResponseEntity<FindNearestDriversResponse> assignNearestDriversInternal(
            @Valid @RequestBody FindNearestDriversRequest request) {

        String tenantId = request.tenantId() != null ? request.tenantId().toString() : null;
        DriverType driverType = request.driverType();

        if (driverType == DriverType.TENANT_DRIVER && tenantId == null) {
            return ResponseEntity.badRequest().build();
        }

        List<String> nearestDrivers = driverLocationService.findNearestDrivers(
                request.pickupLat(),
                request.pickupLon(),
                DEFAULT_K,
                tenantId,
                driverType);
        nearestDrivers = prioritizeExcludedDriverLast(nearestDrivers, request.excludedDriverId());

        Optional<DriverOrderAssignment> assignment = driverLocationService.dispatchNearestDrivers(nearestDrivers,
                request.orderId(), null);

        return ResponseEntity.ok(new FindNearestDriversResponse(
                nearestDrivers,
                assignment.isPresent(),
                assignment.map(DriverOrderAssignment::getDriverId).orElse(null)));
    }

    private List<String> prioritizeExcludedDriverLast(List<String> nearestDrivers, UUID excludedDriverId) {
        if (excludedDriverId == null || nearestDrivers == null || nearestDrivers.isEmpty()) {
            return nearestDrivers;
        }
        List<String> prioritized = new ArrayList<>(nearestDrivers);
        String excluded = excludedDriverId.toString();
        if (!prioritized.remove(excluded)) {
            return prioritized;
        }
        prioritized.add(excluded);
        return prioritized;
    }
}
