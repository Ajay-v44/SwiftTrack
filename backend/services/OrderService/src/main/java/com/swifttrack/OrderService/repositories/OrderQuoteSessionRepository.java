package com.swifttrack.OrderService.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.swifttrack.OrderService.models.OrderQuoteSession;
import com.swifttrack.OrderService.models.enums.QuoteSessionStatus;

@Repository
public interface OrderQuoteSessionRepository extends JpaRepository<OrderQuoteSession, UUID> {

    List<OrderQuoteSession> findByOrderId(UUID orderId);

    // Find active session for an order
    @Query("SELECT qs FROM OrderQuoteSession qs WHERE qs.order.id = :orderId AND qs.status = 'ACTIVE' AND qs.expiresAt > :now")
    Optional<OrderQuoteSession> findActiveSessionByOrder(@Param("orderId") UUID orderId, @Param("now") LocalDateTime now);

    // Find expired active sessions
    List<OrderQuoteSession> findByStatusAndExpiresAtBefore(QuoteSessionStatus status, LocalDateTime dateTime);
}
