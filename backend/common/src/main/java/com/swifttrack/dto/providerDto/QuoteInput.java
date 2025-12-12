package com.swifttrack.dto.providerDto;

public record QuoteInput(
        double pickupLat,
        double pickupLng,
        double dropoffLat,
        double dropoffLng
) {

}
