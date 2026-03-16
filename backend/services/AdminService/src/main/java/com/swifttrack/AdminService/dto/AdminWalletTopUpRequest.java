package com.swifttrack.AdminService.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request for admin wallet top-up operation.
 */
public record AdminWalletTopUpRequest(
        UUID userId,
        BigDecimal amount,
        String note
) {}
