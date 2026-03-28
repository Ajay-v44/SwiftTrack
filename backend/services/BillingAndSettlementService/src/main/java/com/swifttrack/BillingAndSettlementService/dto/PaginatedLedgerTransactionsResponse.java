package com.swifttrack.BillingAndSettlementService.dto;

import java.util.List;

public record PaginatedLedgerTransactionsResponse(
        List<LedgerTransactionListItemResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages) {
}
