package com.swifttrack.OrderService.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TenantOrderListItemDto(
        UUID id,
        String customerReferenceId,
        String orderStatus,
        String pickupCity,
        String dropoffCity,
        String operator,
        LocalDateTime createdAt) {
}
