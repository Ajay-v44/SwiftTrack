package com.swifttrack.DriverService.models;

import java.time.LocalDateTime;
import java.util.UUID;

import com.swifttrack.enums.DriverAssignmentStatus;

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
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "driver_order_assignment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverOrderAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID orderId;
    private UUID driverId;
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    private DriverAssignmentStatus status;

    private LocalDateTime assignedAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.assignedAt == null)
            this.assignedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
