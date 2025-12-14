package com.swifttrack.OrderService.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swifttrack.OrderService.models.OrderProviderAssignment;

@Repository
public interface OrderProviderAssignmentRepository extends JpaRepository<OrderProviderAssignment, UUID> {

    List<OrderProviderAssignment> findByOrderId(UUID orderId);

    // Find assignments by provider
    List<OrderProviderAssignment> findByProviderCode(String providerCode);
    
    // Find latest assignment for order
    OrderProviderAssignment findTopByOrderIdOrderByAssignedAtDesc(UUID orderId);
}
