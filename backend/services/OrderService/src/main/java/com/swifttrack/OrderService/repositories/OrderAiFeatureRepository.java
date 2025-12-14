package com.swifttrack.OrderService.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swifttrack.OrderService.models.OrderAiFeature;

@Repository
public interface OrderAiFeatureRepository extends JpaRepository<OrderAiFeature, UUID> {

    Optional<OrderAiFeature> findByOrderId(UUID orderId);
}
