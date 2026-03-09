package com.swifttrack.BillingAndSettlementService.models;

import com.swifttrack.BillingAndSettlementService.models.enums.MarginRequestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "margin_config_add_request", schema = "billing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarginConfigAddRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "requested_by", nullable = false)
    private UUID requestedBy;

    @Column(name = "key", nullable = false, length = 100)
    private String key;

    @Column(name = "value", nullable = false, precision = 14, scale = 4)
    private BigDecimal value;

    @Column(name = "base_fare", precision = 14, scale = 2)
    private BigDecimal baseFare;

    @Column(name = "per_km_rate", precision = 14, scale = 2)
    private BigDecimal perKmRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MarginRequestStatus status;

    @Column(name = "acted_by")
    private UUID actedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = MarginRequestStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
