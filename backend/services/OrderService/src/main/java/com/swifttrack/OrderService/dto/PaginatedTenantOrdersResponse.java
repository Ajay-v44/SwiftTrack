package com.swifttrack.OrderService.dto;

import java.util.List;

public record PaginatedTenantOrdersResponse(
        List<TenantOrderListItemDto> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        TenantOrdersSummaryDto summary) {
}
