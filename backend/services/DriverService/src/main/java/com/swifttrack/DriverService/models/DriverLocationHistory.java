package com.swifttrack.DriverService.models;

import java.math.BigDecimal;
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
@Table(name = "driver_location_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverLocationHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID driverId;
    private UUID tenantId;

    private BigDecimal latitude;
    private BigDecimal longitude;

    private LocalDateTime recordedAt;
}
