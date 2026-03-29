package com.swifttrack.OrderService.dto;

public record TenantOrdersSummaryDto(
        long processedOrders,
        long openIssues,
        long deliveredOrders,
        long activeOrders) {
}
