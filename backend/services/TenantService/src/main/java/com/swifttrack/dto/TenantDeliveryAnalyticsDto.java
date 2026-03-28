package com.swifttrack.dto;

import java.util.List;

public record TenantDeliveryAnalyticsDto(
        String startDate,
        String endDate,
        long deliveredOrders,
        double averagePerDay,
        long peakDeliveredOrders,
        String peakDate,
        List<TenantDeliveryVolumePointDto> deliveryVolume) {
}
