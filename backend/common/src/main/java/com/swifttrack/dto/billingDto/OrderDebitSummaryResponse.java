package com.swifttrack.dto.billingDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderDebitSummaryResponse(
        UUID accountId,
        UUID orderId,
        BigDecimal debitedAmount,
        LocalDateTime lastDebitedAt,
        String description) {
}
