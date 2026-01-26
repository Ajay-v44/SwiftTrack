package com.swifttrack.OrderService.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swifttrack.OrderService.models.OrderTrackingEvent;
import com.swifttrack.OrderService.models.enums.TrackingStatus;

@Repository
public interface OrderTrackingEventRepository extends JpaRepository<OrderTrackingEvent, UUID> {

        // Get timeline for an order
        List<OrderTrackingEvent> findByOrderIdOrderByEventTimeAsc(UUID orderId);

        // Find specific events
        List<OrderTrackingEvent> findByOrderIdAndStatus(UUID orderId, TrackingStatus status);

        // Check if event exists for status (avoiding loading Order entity)
        boolean existsByOrderIdAndStatus(UUID orderId, TrackingStatus status);

        /**
         * Atomic insert for tracking event - bypasses JPA's Order loading
         * to avoid conflicts with OrderTrackingState updates.
         * Uses PostgreSQL-compatible syntax.
         */
        @org.springframework.data.jpa.repository.Modifying
        @org.springframework.transaction.annotation.Transactional
        @org.springframework.data.jpa.repository.Query(value = "INSERT INTO order_tracking_events (id, order_id, status, latitude, longitude, event_time, description, created_at) "
                        +
                        "SELECT :id, :orderId, :status, :lat, :lng, :eventTime, :description, :createdAt " +
                        "WHERE NOT EXISTS (SELECT 1 FROM order_tracking_events WHERE order_id = :orderId AND status = :status)", nativeQuery = true)
        int insertIfNotExists(
                        @org.springframework.data.repository.query.Param("id") java.util.UUID id,
                        @org.springframework.data.repository.query.Param("orderId") java.util.UUID orderId,
                        @org.springframework.data.repository.query.Param("status") String status,
                        @org.springframework.data.repository.query.Param("lat") java.math.BigDecimal lat,
                        @org.springframework.data.repository.query.Param("lng") java.math.BigDecimal lng,
                        @org.springframework.data.repository.query.Param("eventTime") java.time.LocalDateTime eventTime,
                        @org.springframework.data.repository.query.Param("description") String description,
                        @org.springframework.data.repository.query.Param("createdAt") java.time.LocalDateTime createdAt);
}
