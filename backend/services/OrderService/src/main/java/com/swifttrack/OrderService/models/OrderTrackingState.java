package com.swifttrack.OrderService.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.swifttrack.OrderService.models.enums.TrackingStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_tracking_state")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrackingState {

    @Id
    @Column(name = "order_id")
    private UUID orderId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "order_id")
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", length = 30)
    private TrackingStatus currentStatus;

    @Column(name = "last_latitude", precision = 10, scale = 6)
    private BigDecimal lastLatitude;

    @Column(name = "last_longitude", precision = 10, scale = 6)
    private BigDecimal lastLongitude;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;
}
