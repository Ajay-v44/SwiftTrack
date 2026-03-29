package com.swifttrack.dto.orderDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.swifttrack.dto.billingDto.OrderDebitSummaryResponse;

public record OrderDetailsResponse(
        UUID id,
        UUID tenantId,
        UUID ownerUserId,
        UUID createdBy,
        UUID assignedDriverId,
        String accessScope,
        String customerReferenceId,
        String orderStatus,
        String trackingStatus,
        String bookingChannel,
        String orderType,
        String paymentType,
        BigDecimal paymentAmount,
        String selectedProviderCode,
        String providerOrderId,
        UUID quoteSessionId,
        String selectedType,
        String city,
        String state,
        Double pickupLat,
        Double pickupLng,
        Double dropoffLat,
        Double dropoffLng,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime lastStatusUpdatedAt,
        LocalDateTime lastLocationUpdatedAt,
        OrderLocationInfo pickup,
        OrderLocationInfo dropoff,
        CurrentLocationInfo currentLocation,
        OrderDebitSummaryResponse tenantDebit,
        List<OrderLocationInfo> locations) {

    public record OrderLocationInfo(
            UUID id,
            String type,
            Double latitude,
            Double longitude,
            String city,
            String state,
            String country,
            String pincode,
            String locality,
            LocalDateTime createdAt) {
    }

    public record CurrentLocationInfo(
            String status,
            Double latitude,
            Double longitude,
            LocalDateTime updatedAt) {
    }

    public record OrderTimelineEvent(
            UUID id,
            String providerCode,
            String status,
            Double latitude,
            Double longitude,
            String description,
            LocalDateTime eventTime,
            LocalDateTime createdAt) {
    }
}
