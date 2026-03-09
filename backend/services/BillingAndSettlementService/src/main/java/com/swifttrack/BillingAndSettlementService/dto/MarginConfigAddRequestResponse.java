package com.swifttrack.BillingAndSettlementService.dto;

import com.swifttrack.BillingAndSettlementService.models.enums.MarginRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarginConfigAddRequestResponse {
    private UUID id;
    private UUID tenantId;
    private UUID requestedBy;
    private String key;
    private BigDecimal value;
    private BigDecimal baseFare;
    private BigDecimal perKmRate;
    private MarginRequestStatus status;
    private UUID actedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
