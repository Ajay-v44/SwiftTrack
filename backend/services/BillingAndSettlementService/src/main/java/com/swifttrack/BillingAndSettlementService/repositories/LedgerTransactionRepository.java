package com.swifttrack.BillingAndSettlementService.repositories;

import com.swifttrack.BillingAndSettlementService.models.LedgerTransaction;
import com.swifttrack.BillingAndSettlementService.models.enums.ReferenceType;
import com.swifttrack.BillingAndSettlementService.models.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LedgerTransactionRepository extends JpaRepository<LedgerTransaction, UUID> {

    List<LedgerTransaction> findByAccountId(UUID accountId);

    List<LedgerTransaction> findByOrderId(UUID orderId);

    List<LedgerTransaction> findByReferenceTypeAndReferenceId(ReferenceType referenceType, UUID referenceId);

    Optional<LedgerTransaction> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT COALESCE(SUM(CASE WHEN lt.transactionType = 'CREDIT' THEN lt.amount ELSE 0 END), 0) - " +
           "COALESCE(SUM(CASE WHEN lt.transactionType = 'DEBIT' THEN lt.amount ELSE 0 END), 0) " +
           "FROM LedgerTransaction lt WHERE lt.accountId = :accountId")
    BigDecimal calculateBalanceByAccountId(@Param("accountId") UUID accountId);

    @Query("SELECT lt FROM LedgerTransaction lt WHERE lt.accountId = :accountId ORDER BY lt.createdAt DESC")
    List<LedgerTransaction> findByAccountIdOrderByCreatedAtDesc(@Param("accountId") UUID accountId);
}
