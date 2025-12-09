package com.swifttrack.ProviderService.models;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tenant_provider_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantProviderRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "provider_name", length = 150, nullable = false)
    private String providerName;

    @Column(name = "provider_website", length = 200)
    private String providerWebsite = "";

    @Column(name = "contact_email", length = 150)
    private String contactEmail = "";

    @Column(name = "contact_phone", length = 50)
    private String contactPhone = "";

    @Column(length = 2000)
    private String notes = "";                 // tenant note: why they need it

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String docLinks = "[]";   // API docs, Postman collections, etc.

    @Column(length = 30)
    private String status = "PENDING";  // PENDING, IN_REVIEW, APPROVED, REJECTED

    @Column(name = "rejection_reason", length = 2000)
    private String rejectionReason = "";

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", nullable = false)
    private UUID createdBy;
    
    @Column(name = "updated_by", nullable = false)
    private UUID updatedBy;

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