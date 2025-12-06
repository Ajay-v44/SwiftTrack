package com.swifttrack.Models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // -------------------------
    // Core Tenant Information
    // -------------------------
    @Column(nullable = false, unique = true)
    private String tenantCode; // example: "freshivores", "zeelog"

    @Column(nullable = false)
    private String organizationName;

    private String organizationAddress = "";
    private String organizationPhone = "";
    private String organizationEmail = "";
    private String organizationWebsite = "";
    private String organizationState = "";
    private String organizationCity = "";
    private String organizationCountry = "";

    // -------------------------
    // Statutory Information
    // -------------------------
    @Column(name = "gst_number")
    private String gstNumber = ""; // GSTIN

    @Column(name = "cin_number")
    private String cinNumber = ""; // Corporate Identification Number (optional)

    @Column(name = "pan_number")
    private String panNumber = ""; // PAN for billing

    // -------------------------
    // Subscription / Billing
    // -------------------------
    @Enumerated(EnumType.STRING)
    private PlanType planType = PlanType.FREE;

    private Integer maxDrivers = 100;
    private Integer maxOrdersPerDay = 1000;

    @Enumerated(EnumType.STRING)
    private BillingCycle billingCycle = BillingCycle.MONTHLY;

    // -------------------------
    // Dispatch / Operations Settings
    // -------------------------
    private Integer defaultSlaMinutes = 30;

    private boolean enableAutoAssignment = true;

    @Enumerated(EnumType.STRING)
    private AssignmentStrategy assignmentStrategy = AssignmentStrategy.HYBRID;

    private Integer geofenceStrictness = 2; // 1–5 scale

    // -------------------------
    // Responsible AI Settings
    // -------------------------
    @Enumerated(EnumType.STRING)
    private AIPolicyLevel aiPolicyLevel = AIPolicyLevel.STANDARD;

    private boolean enableHumanInTheLoop = false;

    private String modelVersion = "v1.0"; // ML model version used

    // -------------------------
    // Integrations
    // -------------------------
    private String externalApiKey = "";

    private String webhookUrl = "";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String integrationSettings = "{}"; // dynamic config as JSON

    // -------------------------
    // Branding
    // -------------------------
    private String logoUrl = "";
    private String themeColor = "#3A86FF";

    // -------------------------
    // Security / Compliance
    // -------------------------
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String ipWhitelist = "[]";

    private Integer dataRetentionDays = 90;
    private boolean auditLoggingEnabled = true;

    // -------------------------
    // Analytics
    // -------------------------
    private LocalDateTime lastActiveAt = LocalDateTime.now();
    private Long totalOrdersProcessed = 0L;

    // -------------------------
    // Lifecycle Management
    // -------------------------
    @Enumerated(EnumType.STRING)
    private TenantStatus status = TenantStatus.PENDING_VERIFICATION;

    // -------------------------
    // Timestamps
    // -------------------------
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null)
            status = TenantStatus.PENDING_VERIFICATION;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // -------------------------
    // Enums
    // -------------------------
    public enum TenantStatus {
        ACTIVE,
        INACTIVE,
        PENDING_VERIFICATION,
        SUSPENDED
    }

    public enum PlanType {
        FREE,
        PRO,
        ENTERPRISE
    }

    public enum BillingCycle {
        MONTHLY,
        YEARLY
    }

    public enum AssignmentStrategy {
        DISTANCE_ONLY,
        BEHAVIOUR_MODEL,
        HYBRID
    }

    public enum AIPolicyLevel {
        STANDARD,
        STRICT,
        HUMAN_REVIEW_REQUIRED
    }
}
