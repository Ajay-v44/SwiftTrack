package com.swifttrack.BillingAndSettlementService.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record LedgerTransactionListItemResponse(
        UUID id,
        String description,
        LocalDateTime createdAt,
        BigDecimal amount,
        String transactionType,
        String referenceType,
        UUID orderId) {
}
