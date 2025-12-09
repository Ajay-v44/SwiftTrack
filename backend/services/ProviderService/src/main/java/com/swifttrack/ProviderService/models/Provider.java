package com.swifttrack.ProviderService.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "provider")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Provider {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "provider_name", length = 100, nullable = false)
    private String providerName;          // e.g., "Shadowfax", "Rapido"

    @Column(name = "provider_code", length = 50, nullable = false, unique = true)
    private String providerCode;    // internal code like "SHADOWFAX", "RAPIDO"

    @Column(length = 1000)
    private String description = "";

    @Column(length = 1000)
    private String logoUrl = "";

    @Column(length = 1000)
    private String websiteUrl = "";

    // Supported service types
    @Column(nullable = false)
    private boolean supportsHyperlocal = false;

    @Column(nullable = false)
    private boolean supportsCourier = false;

    @Column(nullable = false)
    private boolean supportsSameDay = false;

    @Column(nullable = false)
    private boolean supportsIntercity = false;

    // API configuration template
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String configSchema = "{}";      // what fields tenant must provide

    // Operational settings
    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false)
    private boolean allowSelfRegistration = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Column(nullable = false)
    private UUID createdById;
    
    @Column(nullable = false)
    private UUID updatedBy;

    @OneToMany(mappedBy = "provider")
    private List<ProviderServicableAreas> providerServicableAreas;
    
    @OneToMany(mappedBy = "provider")
    private List<TenantProviderConfig> tenantProviderConfigs;

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