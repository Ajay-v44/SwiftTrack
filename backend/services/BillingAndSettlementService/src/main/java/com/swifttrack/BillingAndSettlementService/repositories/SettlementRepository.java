package com.swifttrack.BillingAndSettlementService.repositories;

import com.swifttrack.BillingAndSettlementService.models.Settlement;
import com.swifttrack.BillingAndSettlementService.models.enums.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, UUID> {

    List<Settlement> findByAccountId(UUID accountId);

    List<Settlement> findByAccountIdAndStatus(UUID accountId, SettlementStatus status);

    List<Settlement> findByStatus(SettlementStatus status);
}
