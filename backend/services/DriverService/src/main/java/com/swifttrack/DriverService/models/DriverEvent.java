package com.swifttrack.DriverService.models;

import java.time.LocalDateTime;
import java.util.UUID;

import com.swifttrack.enums.DriverEventType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "driver_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID driverId;
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    private DriverEventType eventType;

    @Column(columnDefinition = "TEXT") // JSON support varies, using TEXT for simplicity
    private String metadata;

    private LocalDateTime createdAt;
}
