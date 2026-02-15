package com.swifttrack.DriverService.dto;

import java.util.UUID;

import com.swifttrack.enums.TrackingStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateOrderStatusrequest(
                @Schema(description = "Order tracking status", allowableValues = {
                                "PICKED_UP", "IN_TRANSIT",
                                "OUT_FOR_DELIVERY",
                                "DELIVERED", "FAILED" }) TrackingStatus status,
                UUID orderId) {

}
