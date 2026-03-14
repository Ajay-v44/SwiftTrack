package com.swifttrack.dto.orderDto;

import java.math.BigDecimal;

public record OrderQuoteSessionResponse(
                String providerCode,
                String selectedType,
                BigDecimal price,
                String currency,
                BigDecimal aiScore,
                String quoteId) {

}
