package com.swifttrack.OrderService.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.swifttrack.OrderService.models.enums.QuoteSessionStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_quote_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderQuoteSession {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private QuoteSessionStatus status = QuoteSessionStatus.ACTIVE;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "quoteSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderQuote> quotes;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
