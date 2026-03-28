package com.swifttrack.BillingAndSettlementService.dto;

import java.math.BigDecimal;

public record FinanceSummaryResponse(
        BigDecimal balance,
        BigDecimal weeklySpend,
        BigDecimal costSavings,
        BigDecimal unpaidDues,
        long invoiceCount) {
}
