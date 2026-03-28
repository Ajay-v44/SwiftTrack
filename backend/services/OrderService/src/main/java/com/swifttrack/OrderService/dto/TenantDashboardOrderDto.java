package com.swifttrack.OrderService.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TenantDashboardOrderDto(
        UUID id,
        String customerReferenceId,
        String orderStatus,
        String city,
        LocalDateTime createdAt) {
}
