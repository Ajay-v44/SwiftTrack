package com.swifttrack.AdminService.dto;

import java.util.UUID;

/**
 * Request to cancel an order with admin reason.
 */
public record AdminCancelOrderRequest(
        UUID orderId,
        String reason
) {}
