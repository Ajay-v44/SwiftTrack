package com.swifttrack.dto.orderDto;

import java.math.BigDecimal;
import java.util.UUID;

public record FinalCreateOrderResponse(
        UUID orderId,
        String providerCode,
        BigDecimal totalAmount) {

}
