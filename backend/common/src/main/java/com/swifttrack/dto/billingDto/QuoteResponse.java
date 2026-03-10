package com.swifttrack.dto.billingDto;

import java.math.BigDecimal;

public record QuoteResponse(
        BigDecimal driverCost,
        BigDecimal platformMargin,
        BigDecimal providerCost,
        BigDecimal tenantCharge) {
}
