package com.swifttrack.dto.providerDto;

import java.util.UUID;

public record QuoteInput(
        Double pickupLat,
        Double pickupLng,
        Double dropoffLat,
        Double dropoffLng,
        UUID pickupAddressId
) {

}
