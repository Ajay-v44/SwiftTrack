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
        OrderDetailsResponse.OrderLocationInfo pickup,
        OrderDetailsResponse.OrderLocationInfo dropoff,
        List<OrderDetailsResponse.OrderLocationInfo> locations,
        List<OrderDetailsResponse.OrderTimelineEvent> events) {
}
