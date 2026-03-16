package com.swifttrack.OrderService.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.swifttrack.OrderService.models.enums.AddressOwnerType;

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
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "owner_user_id")
    private UUID ownerUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 20)
    private AddressOwnerType ownerType;

    @Column(name = "label", length = 120)
    private String label;

    @Column(name = "line_1", nullable = false, length = 255)
    private String line1;

    @Column(name = "line_2", length = 255)
    private String line2;

    @Column(name = "city", nullable = false, length = 120)
    private String city;

    @Column(name = "state", nullable = false, length = 120)
    private String state;

    @Column(name = "country", nullable = false, length = 120)
    private String country;

    @Column(name = "pincode", nullable = false, length = 40)
    private String pincode;

    @Column(name = "locality", length = 120)
    private String locality;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(name = "contact_name", nullable = false, length = 120)
    private String contactName;

    @Column(name = "contact_phone", nullable = false, length = 40)
    private String contactPhone;

    @Column(name = "business_name", length = 120)
    private String businessName;

    @Column(name = "notes", length = 255)
    private String notes;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
