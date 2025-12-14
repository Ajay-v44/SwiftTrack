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

@Entity
@Table(name = "order_ai_features")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderAiFeature {

    @Id
    @Column(name = "order_id")
    private UUID orderId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "distance_km", precision = 6, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "traffic_level")
    private Integer trafficLevel;

    @Column(name = "is_peak_hour")
    private Boolean isPeakHour;

    @Column(name = "provider_load")
    private Integer providerLoad;

    @Column(name = "predicted_eta")
    private Integer predictedEta;

    @Column(name = "actual_eta")
    private Integer actualEta;

    @Column(name = "success")
    private Boolean success;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;
}
