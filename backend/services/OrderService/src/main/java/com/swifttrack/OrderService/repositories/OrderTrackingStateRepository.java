package com.swifttrack.OrderService.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swifttrack.OrderService.models.OrderTrackingState;

@Repository
public interface OrderTrackingStateRepository extends JpaRepository<OrderTrackingState, UUID> {
    
    Optional<OrderTrackingState> findByOrderId(UUID orderId);
}
