package com.swifttrack.OrderService.dto;

import java.util.List;

public record TenantDeliveryAnalyticsDto(
        String startDate,
        String endDate,
        long deliveredOrders,
        double averagePerDay,
        long peakDeliveredOrders,
        String peakDate,
        List<TenantDashboardVolumePointDto> deliveryVolume) {
}
