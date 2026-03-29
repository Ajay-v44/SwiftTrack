package com.swifttrack.OrderService.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DriverLocationSnapshot(
        UUID driverId,
        UUID tenantId,
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDateTime updatedAt) {
}
