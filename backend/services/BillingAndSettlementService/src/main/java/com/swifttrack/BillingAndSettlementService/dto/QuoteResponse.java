package com.swifttrack.BillingAndSettlementService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteResponse {
    private BigDecimal driverCost;
    private BigDecimal platformMargin;
    private BigDecimal providerCost;
    private BigDecimal tenantCharge;
}
