package com.swifttrack.OrderService.dto;

import java.util.UUID;

public record AddressQuoteRequest(
        UUID pickupAddressId,
        Double dropoffLat,
        Double dropoffLng) {
}
