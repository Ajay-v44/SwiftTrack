package com.swifttrack.Models;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "companie")
@NoArgsConstructor
@AllArgsConstructor
public class CompanyModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name="organization_name")
    private String organizationName;
    @Column(name="organization_address")
    private String organizationAddress;
    @Column(name="organization_phone")
    private String organizationPhone;
    @Column(name="organization_email")
    private String organizationEmail;
    @Column(name="organization_website")
    private String organizationWebsite;
    @Column(name="status")
    private boolean status;

    
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
