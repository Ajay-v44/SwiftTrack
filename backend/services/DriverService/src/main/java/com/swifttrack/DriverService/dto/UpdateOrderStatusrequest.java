package com.swifttrack.DriverService.dto;

import java.util.UUID;

import com.swifttrack.enums.TrackingStatus;

public record UpdateOrderStatusrequest(
        TrackingStatus status,
        UUID orderId) {

}
