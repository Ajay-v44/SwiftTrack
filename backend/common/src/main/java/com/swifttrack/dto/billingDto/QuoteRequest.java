package com.swifttrack.dto.billingDto;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public record QuoteRequest(
        UUID quoteSessionId,
        Optional<BigDecimal> price,
        Optional<String> providerCode,
        Optional<BigDecimal> distance,
        UUID userId,
        String selectedType
) {
}
