package com.swifttrack.dto;

import java.util.UUID;

public record DeliveryOptionResponse(
        UUID id,
        String optionType,
        boolean active) {
}
