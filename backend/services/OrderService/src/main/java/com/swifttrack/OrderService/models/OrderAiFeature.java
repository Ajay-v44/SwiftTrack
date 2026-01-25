package com.swifttrack.OrderService.models;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.domain.Persistable;
import jakarta.persistence.Transient;

@Entity
@Table(name = "order_ai_features")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderAiFeature implements Persistable<UUID> {

    @Id
    @Column(name = "order_id")
    private UUID orderId;

    @Transient
    private boolean isNew = true;

    @Override
    public UUID getId() {
        return orderId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
        this.isNew = true; // Ensure it's treated as new when ID is set manually
    }

    @OneToOne
    @MapsId
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "distance_km", precision = 6, scale = 2, nullable = false)
    private BigDecimal distanceKm;

    @Column(name = "traffic_level", nullable = true)
    private Integer trafficLevel;

    @Column(name = "is_peak_hour", nullable = true)
    private Boolean isPeakHour;

    @Column(name = "provider_load", nullable = true)
    private Integer providerLoad;

    @Column(name = "predicted_eta", nullable = true)
    private Integer predictedEta;

    @Column(name = "actual_eta", nullable = true)
    private Integer actualEta;

    @Column(name = "success", nullable = true)
    private Boolean success;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT", nullable = true)
    private String cancellationReason;
}
