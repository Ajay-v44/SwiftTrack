package com.swifttrack.BillingAndSettlementService.repositories;

import com.swifttrack.BillingAndSettlementService.models.PricingSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PricingSnapshotRepository extends JpaRepository<PricingSnapshot, UUID> {

    Optional<PricingSnapshot> findByOrderId(UUID orderId);
}
