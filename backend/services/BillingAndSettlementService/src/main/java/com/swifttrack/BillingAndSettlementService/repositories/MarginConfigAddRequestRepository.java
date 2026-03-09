package com.swifttrack.BillingAndSettlementService.repositories;

import com.swifttrack.BillingAndSettlementService.models.MarginConfigAddRequest;
import com.swifttrack.BillingAndSettlementService.models.enums.MarginRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MarginConfigAddRequestRepository extends JpaRepository<MarginConfigAddRequest, UUID> {

    Optional<MarginConfigAddRequest> findFirstByTenantIdAndStatusOrderByUpdatedAtDesc(UUID tenantId,
            MarginRequestStatus status);

    List<MarginConfigAddRequest> findByStatusOrderByCreatedAtDesc(MarginRequestStatus status);

    List<MarginConfigAddRequest> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    List<MarginConfigAddRequest> findAllByOrderByCreatedAtDesc();

}
