package com.swifttrack.OrderService.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.swifttrack.OrderService.models.enums.QuoteSessionStatus;
import com.swifttrack.enums.BillingAndSettlement.BookingChannel;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "owner_user_id")
    private UUID ownerUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_channel", length = 20)
    private BookingChannel bookingChannel;

    @Column(name = "guest_access_token", length = 120)
    private String guestAccessToken;

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
