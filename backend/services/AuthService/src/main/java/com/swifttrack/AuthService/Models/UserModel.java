package com.swifttrack.AuthService.Models;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class UserModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "tenant_id")
    private UUID tenantId;
    
    @Column(name = "provider_id")
    private UUID providerId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private UserType type;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "mobile")
    private String mobile;
    
    @Column(name = "password_hash")
    private String passwordHash;
    
    @Column(name = "otp")
    private String otp;
    
    @Column(name = "last_otp_sent_at")
    private LocalDateTime lastOtpSentAt;
    
    @Column(name = "status")
    private Boolean status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status")
    private VerificationStatus verificationStatus;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}

enum UserType {
    TENANT_USER, DRIVER_USER, PROVIDER_USER, ADMIN_USER
}

enum VerificationStatus {
    PENDING, APPROVED, REJECTED
}