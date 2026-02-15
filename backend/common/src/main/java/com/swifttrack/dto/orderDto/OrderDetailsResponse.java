package com.swifttrack.dto.orderDto;

import java.util.UUID;

public record OrderDetailsResponse(
        UUID id,
        String customerReferenceId,
        String orderStatus,
        String city,
        String state,
        Double pickupLat,
        Double pickupLng,
        Double dropoffLat,
        Double dropoffLng) {
}
