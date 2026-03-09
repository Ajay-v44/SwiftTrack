package com.swifttrack.BillingAndSettlementService.models;

import com.swifttrack.BillingAndSettlementService.models.enums.MarginType;
import com.swifttrack.BillingAndSettlementService.models.enums.OrganizationType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "margin_config", schema = "billing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarginConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "organization_type", nullable = false, length = 20)
    private OrganizationType organizationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "margin_type", nullable = false, length = 20)
    private MarginType marginType;

    @Column(name = "key", nullable = false, length = 100)
    private String key;

    @Column(name = "value", nullable = false, precision = 14, scale = 4)
    private BigDecimal value;

    @Column(name = "base_fare", precision = 14, scale = 2)
    private BigDecimal baseFare;

    @Column(name = "per_km_rate", precision = 14, scale = 2)
    private BigDecimal perKmRate;

    @Column(name = "commission_percent", precision = 7, scale = 4)
    private BigDecimal commissionPercent;

    @Column(name = "minimum_platform_fee", precision = 14, scale = 2)
    private BigDecimal minimumPlatformFee;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) isActive = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
