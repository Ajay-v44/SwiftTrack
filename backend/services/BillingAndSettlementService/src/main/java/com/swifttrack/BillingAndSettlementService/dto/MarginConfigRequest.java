package com.swifttrack.BillingAndSettlementService.dto;

import com.swifttrack.BillingAndSettlementService.models.enums.MarginType;
import com.swifttrack.BillingAndSettlementService.models.enums.OrganizationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarginConfigRequest {
    private UUID userId;
    private OrganizationType organizationType;
    private MarginType marginType;
    private String key;
    private BigDecimal value;
    private BigDecimal baseFare;
    private BigDecimal perKmRate;
    private BigDecimal commissionPercent;
    private BigDecimal minimumPlatformFee;
}
