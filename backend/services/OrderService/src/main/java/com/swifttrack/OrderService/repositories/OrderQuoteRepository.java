package com.swifttrack.OrderService.repositories;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.swifttrack.OrderService.models.OrderQuote;

@Repository
public interface OrderQuoteRepository extends JpaRepository<OrderQuote, UUID> {

    List<OrderQuote> findByQuoteSessionId(UUID quoteSessionId);

    // Find selected quote for a session
    Optional<OrderQuote> findByQuoteSessionIdAndIsSelectedTrue(UUID quoteSessionId);

    // Find cheapest quote in a session
    @Query("SELECT q FROM OrderQuote q WHERE q.quoteSession.id = :sessionId ORDER BY q.price ASC LIMIT 1")
    Optional<OrderQuote> findCheapestQuote(@Param("sessionId") UUID sessionId);

    // Find fastest pickup quote
    @Query("SELECT q FROM OrderQuote q WHERE q.quoteSession.id = :sessionId ORDER BY q.estimatedPickupMinutes ASC LIMIT 1")
    Optional<OrderQuote> findFastestPickupQuote(@Param("sessionId") UUID sessionId);
}
