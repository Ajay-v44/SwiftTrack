package com.swifttrack.BillingAndSettlementService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteRequest {
    private UUID quoteSessionId;
    private Optional<BigDecimal> price;
    private Optional<String> providerCode;
    private Optional<BigDecimal> distance;
    private UUID userId;
    private String selectedType;
}
