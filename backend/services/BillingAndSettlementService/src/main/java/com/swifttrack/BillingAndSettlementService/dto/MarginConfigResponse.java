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
public class MarginConfigResponse {
    private String key;
    private BigDecimal value;
    private BigDecimal minimumPlatformFee;
    private BigDecimal baseFare;
    private BigDecimal perKmRate;
    private BigDecimal commissionPercent;
}
