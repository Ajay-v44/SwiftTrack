package com.swifttrack.dto.orderDto;

import java.util.List;
import java.util.UUID;

public record DeliveryOptionsQuoteResponse(
        UUID quoteSessionId,
        List<DeliveryOptionQuote> options,
        String guestAccessToken) {
}
