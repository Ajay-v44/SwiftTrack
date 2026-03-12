package com.swifttrack.OrderService.dto;

import java.util.UUID;

public record AssignNearestDriversRequest(
        double pickupLat,
        double pickupLon,
        UUID orderId,
        String driverType,
        UUID tenantId,
        UUID excludedDriverId) {
}
