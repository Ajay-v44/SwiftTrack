package com.swifttrack.dto.orderDto;

import java.util.UUID;

import com.swifttrack.dto.providerDto.QuoteResponse;

public record OrderQuoteResponse(
        QuoteResponse quoteResponse,
        UUID quoteSessionId) {

}
