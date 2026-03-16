package com.swifttrack.AdminService.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Audit log for all admin actions performed in the system.
 * Every API call that mutates data should create an entry here for traceability.
 */
@Entity
@Table(name = "admin_audit_log", schema = "admin",
        indexes = {
                @Index(name = "idx_audit_admin_id", columnList = "admin_id"),
                @Index(name = "idx_audit_created_at", columnList = "created_at"),
                @Index(name = "idx_audit_action_type", columnList = "action_type"),
                @Index(name = "idx_audit_target_id", columnList = "target_id")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "admin_id", nullable = false)
    private UUID adminId;

    @Column(name = "admin_name")
    private String adminName;

    @Column(name = "admin_type", length = 50)
    private String adminType;

    /**
     * e.g. USER_STATUS_UPDATE, DRIVER_APPROVE, ORDER_CANCEL, WALLET_TOPUP, etc.
     */
    @Column(name = "action_type", nullable = false, length = 100)
    private String actionType;

    /**
     * The service domain this action belongs to e.g. USER, DRIVER, ORDER, BILLING
     */
    @Column(name = "service_domain", length = 50)
    private String serviceDomain;

    /**
     * UUID of the entity being acted upon (user, driver, order, etc.)
     */
    @Column(name = "target_id")
    private UUID targetId;

    @Column(name = "target_type", length = 100)
    private String targetType;

    /**
     * Full JSON description of what changed.
     */
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
