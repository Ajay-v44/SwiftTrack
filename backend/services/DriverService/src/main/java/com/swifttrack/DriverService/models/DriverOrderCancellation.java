package com.swifttrack.DriverService.models;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "driver_order_cancellation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverOrderCancellation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID orderId;
    private UUID driverId;
    private UUID tenantId;

    private String reason;

    private LocalDateTime cancelledAt;
}
