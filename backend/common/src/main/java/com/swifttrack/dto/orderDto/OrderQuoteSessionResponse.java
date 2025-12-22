package com.swifttrack.dto.orderDto;

import java.math.BigDecimal;

public record OrderQuoteSessionResponse(
                String providerCode,
                String currency,
                BigDecimal aiScore) {

}
