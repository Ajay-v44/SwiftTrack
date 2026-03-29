package com.swifttrack.dto.orderDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderTrackingTimelineResponse(
        UUID orderId,
        String orderStatus,
        String trackingStatus,
        LocalDateTime lastStatusUpdatedAt,
        LocalDateTime lastLocationUpdatedAt,
        OrderDetailsResponse.CurrentLocationInfo currentLocation,
        List<OrderDetailsResponse.OrderTimelineEvent> events) {
}
