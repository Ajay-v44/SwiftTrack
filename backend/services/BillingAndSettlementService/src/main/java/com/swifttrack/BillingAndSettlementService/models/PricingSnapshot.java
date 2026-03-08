package com.swifttrack.BillingAndSettlementService.models;

import com.swifttrack.BillingAndSettlementService.models.enums.PricingSource;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pricing_snapshots", schema = "billing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(name = "provider_cost", precision = 14, scale = 2)
    private BigDecimal providerCost;

    @Column(name = "driver_cost", precision = 14, scale = 2)
    private BigDecimal driverCost;

    @Column(name = "platform_margin", nullable = false, precision = 14, scale = 2)
    private BigDecimal platformMargin;

    @Column(name = "tenant_charge", nullable = false, precision = 14, scale = 2)
    private BigDecimal tenantCharge;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_source", nullable = false, length = 20)
    private PricingSource pricingSource;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
