package com.swifttrack.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published when an order reaches DELIVERED status.
 * Consumed by BillingAndSettlementService to trigger billing.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDeliveredEvent {
    private UUID orderId;
    private String tenantId;
    private String providerCode;
    private BigDecimal amount;
    private String orderType; // HYPERLOCAL, INTERCITY
    private String deliverySource; // EXTERNAL_PROVIDER, TENANT_DRIVER, GIG_DRIVER
    private String driverName;
    private UUID driverId;
    private BigDecimal distanceKm;
    private LocalDateTime deliveredAt;
}
