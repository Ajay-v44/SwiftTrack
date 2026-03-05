package com.swifttrack.DriverService.dto.spatial;

import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FindNearestDriversRequest(
        @Min(-90) @Max(90) double pickupLat,
        @Min(-180) @Max(180) double pickupLon,
        @NotNull UUID orderId) {
}
