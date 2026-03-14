package com.swifttrack.dto.orderDto;

import java.util.UUID;

import com.swifttrack.dto.providerDto.QuoteResponse;

public record DeliveryOptionQuote(
        UUID quoteOptionId,
        String choiceCode,
        String selectedType,
        String providerCode,
        QuoteResponse quoteResponse) {
}
