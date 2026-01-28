package com.swifttrack.DriverService.models;

import java.time.LocalDateTime;
import java.util.UUID;

import com.swifttrack.enums.DriverOnlineStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "driver_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverStatus {
    @Id
    private UUID driverId;

    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    private DriverOnlineStatus status;

    private LocalDateTime lastSeenAt;
}
