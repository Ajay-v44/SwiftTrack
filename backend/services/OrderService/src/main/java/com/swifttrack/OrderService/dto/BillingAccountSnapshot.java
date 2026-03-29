package com.swifttrack.OrderService.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BillingAccountSnapshot(
        UUID id,
        UUID userId,
        String accountType,
        BigDecimal balance,
        String currency,
        Boolean isActive) {
}
