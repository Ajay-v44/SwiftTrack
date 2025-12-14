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
}
