package com.swifttrack.OrderService.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swifttrack.OrderService.models.OrderTrackingState;

@Repository
public interface OrderTrackingStateRepository extends JpaRepository<OrderTrackingState, UUID> {

        Optional<OrderTrackingState> findByOrderId(UUID orderId);

        @org.springframework.data.jpa.repository.Modifying
        @org.springframework.transaction.annotation.Transactional
        @org.springframework.data.jpa.repository.Query("UPDATE OrderTrackingState s SET s.currentStatus = :status, s.lastLatitude = :lat, s.lastLongitude = :lng, s.lastUpdatedAt = :time WHERE s.orderId = :orderId")
        int updateTrackingState(
                        @org.springframework.data.repository.query.Param("orderId") UUID orderId,
                        @org.springframework.data.repository.query.Param("status") com.swifttrack.OrderService.models.enums.TrackingStatus status,
                        @org.springframework.data.repository.query.Param("lat") java.math.BigDecimal lat,
                        @org.springframework.data.repository.query.Param("lng") java.math.BigDecimal lng,
                        @org.springframework.data.repository.query.Param("time") java.time.LocalDateTime time);

        /**
         * Atomic insert using native SQL INSERT ON CONFLICT (upsert).
         * This handles race conditions when multiple threads try to insert
         * simultaneously.
         * Returns 1 if inserted, 0 if updated due to conflict.
         */
        @org.springframework.data.jpa.repository.Modifying
        @org.springframework.transaction.annotation.Transactional
        @org.springframework.data.jpa.repository.Query(value = "INSERT INTO order_tracking_state (order_id, current_status, last_latitude, last_longitude, last_updated_at) "
                        +
                        "VALUES (:orderId, :status, :lat, :lng, :time) " +
                        "ON CONFLICT (order_id) DO UPDATE SET current_status = :status, last_latitude = :lat, last_longitude = :lng, last_updated_at = :time", nativeQuery = true)
        int upsertTrackingState(
                        @org.springframework.data.repository.query.Param("orderId") java.util.UUID orderId,
                        @org.springframework.data.repository.query.Param("status") String status,
                        @org.springframework.data.repository.query.Param("lat") java.math.BigDecimal lat,
                        @org.springframework.data.repository.query.Param("lng") java.math.BigDecimal lng,
                        @org.springframework.data.repository.query.Param("time") java.time.LocalDateTime time);
}
