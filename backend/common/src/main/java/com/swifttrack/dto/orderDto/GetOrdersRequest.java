package com.swifttrack.dto.orderDto;

import java.util.List;
import java.util.UUID;

public record GetOrdersRequest(List<UUID> orderIds) {
}
