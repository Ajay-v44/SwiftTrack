package com.swifttrack.dto.orderDto;

import java.math.BigDecimal;

public record CreateOrderResponse(
                String orderId,
                String providerCode,
                BigDecimal totalAmount) {

}
