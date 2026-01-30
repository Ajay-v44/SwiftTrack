package com.swifttrack.DriverService.models;

import java.time.LocalDateTime;
import java.util.UUID;

import com.swifttrack.enums.VehicleType;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "driver_vehicle_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverVehicleDetails {
    @Id
    private UUID driverId; // Matches Auth User ID

    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;

    private String licenseNumber;
    private String driverLicensNumber;

    private Boolean isActive;
    private Boolean isVerified;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isActive == null)
            this.isActive = false;
        if (this.isVerified == null)
            this.isVerified = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
