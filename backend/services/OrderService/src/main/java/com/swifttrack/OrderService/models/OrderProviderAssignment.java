package com.swifttrack.OrderService.models;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.swifttrack.OrderService.models.enums.AssignmentType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_provider_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderProviderAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "provider_code", length = 50)
    private String providerCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_type", length = 20)
    private AssignmentType assignmentType;

    @Column(name = "assignment_reason", columnDefinition = "TEXT")
    private String assignmentReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_decision_snapshot", columnDefinition = "jsonb")
    private String aiDecisionSnapshot;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @PrePersist
    protected void onCreate() {
        assignedAt = LocalDateTime.now();
    }
}
