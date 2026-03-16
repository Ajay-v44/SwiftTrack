package com.swifttrack.AdminService.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request to create or update a margin configuration.
 */
public record AdminMarginConfigRequest(
        UUID userId,
        String marginType,
        BigDecimal marginValue,
        boolean active
) {}
