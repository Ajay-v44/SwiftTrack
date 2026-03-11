package com.swifttrack.dto.billingDto;

import java.util.UUID;

public record BindQuoteOrderRequest(
        UUID quoteSessionId,
        UUID orderId) {
}
