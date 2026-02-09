package com.swifttrack.dto.orderDto;

import java.util.UUID;

public record GetOrdersForDriver(
        UUID id,
        String customerReferenceId,
        String city,
        String state,
        Double pickupLat,
        Double pickupLng,
        Double dropoffLat,
        Double dropoffLng) {

}
