package com.swifttrack.OrderService.dto;

import java.util.List;

public record TenantDashboardSummaryDto(
        long totalDeliveredOrders,
        long activeOrders,
        List<TenantDashboardVolumePointDto> deliveryVolume,
        List<TenantDashboardOrderDto> latestOrders) {
}
