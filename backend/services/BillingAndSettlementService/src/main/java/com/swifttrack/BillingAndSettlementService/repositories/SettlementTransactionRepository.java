package com.swifttrack.BillingAndSettlementService.repositories;

import com.swifttrack.BillingAndSettlementService.models.SettlementTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SettlementTransactionRepository extends JpaRepository<SettlementTransaction, UUID> {

    List<SettlementTransaction> findBySettlementId(UUID settlementId);

    List<SettlementTransaction> findByLedgerTransactionId(UUID ledgerTransactionId);
}
