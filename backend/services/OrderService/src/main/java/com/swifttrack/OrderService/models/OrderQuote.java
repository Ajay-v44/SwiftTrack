package com.swifttrack.OrderService.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "order_quotes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderQuote {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "quote_session_id")
    private OrderQuoteSession quoteSession;

    @Column(name = "provider_code", length = 50)
    private String providerCode;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 10)
    private String currency;

    @Column(name = "estimated_pickup_minutes")
    private Integer estimatedPickupMinutes;

    @Column(name = "estimated_delivery_minutes")
    private Integer estimatedDeliveryMinutes;

    @Column(name = "ai_score", precision = 8, scale = 2)
    private BigDecimal aiScore;

    @Column(name = "is_selected")
    private Boolean isSelected = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
