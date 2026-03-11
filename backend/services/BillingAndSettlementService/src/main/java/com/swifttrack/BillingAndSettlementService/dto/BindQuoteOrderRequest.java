package com.swifttrack.BillingAndSettlementService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BindQuoteOrderRequest {
    private UUID quoteSessionId;
    private UUID orderId;
}
