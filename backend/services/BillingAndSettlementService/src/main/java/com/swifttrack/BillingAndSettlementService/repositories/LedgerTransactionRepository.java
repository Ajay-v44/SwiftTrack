package com.swifttrack.BillingAndSettlementService.repositories;

import com.swifttrack.BillingAndSettlementService.models.LedgerTransaction;
import com.swifttrack.BillingAndSettlementService.models.enums.ReferenceType;
import com.swifttrack.BillingAndSettlementService.models.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    Page<LedgerTransaction> findByAccountIdOrderByCreatedAtDesc(UUID accountId, Pageable pageable);

    Optional<LedgerTransaction> findFirstByAccountIdAndOrderIdAndTransactionTypeAndReferenceTypeOrderByCreatedAtDesc(
            UUID accountId,
            UUID orderId,
            TransactionType transactionType,
            ReferenceType referenceType);

    @Query("SELECT COALESCE(SUM(lt.amount), 0) FROM LedgerTransaction lt " +
           "WHERE lt.accountId = :accountId AND lt.transactionType = :transactionType AND lt.createdAt >= :fromDateTime")
    BigDecimal sumAmountByAccountIdAndTransactionTypeSince(
            @Param("accountId") UUID accountId,
            @Param("transactionType") TransactionType transactionType,
            @Param("fromDateTime") LocalDateTime fromDateTime);

    @Query("SELECT COALESCE(SUM(lt.amount), 0) FROM LedgerTransaction lt " +
           "WHERE lt.accountId = :accountId AND lt.transactionType = :transactionType " +
           "AND lt.referenceType = :referenceType AND lt.createdAt >= :fromDateTime")
    BigDecimal sumAmountByAccountIdAndTransactionTypeAndReferenceTypeSince(
            @Param("accountId") UUID accountId,
            @Param("transactionType") TransactionType transactionType,
            @Param("referenceType") ReferenceType referenceType,
            @Param("fromDateTime") LocalDateTime fromDateTime);

    @Query("SELECT COALESCE(SUM(lt.amount), 0) FROM LedgerTransaction lt " +
           "WHERE lt.accountId = :accountId AND lt.transactionType = :transactionType " +
           "AND lt.referenceType = :referenceType")
    BigDecimal sumAmountByAccountIdAndTransactionTypeAndReferenceType(
            @Param("accountId") UUID accountId,
            @Param("transactionType") TransactionType transactionType,
            @Param("referenceType") ReferenceType referenceType);

    long countByAccountIdAndTransactionTypeAndReferenceType(
            UUID accountId,
            TransactionType transactionType,
            ReferenceType referenceType);

    @Query("SELECT COALESCE(SUM(lt.amount), 0) FROM LedgerTransaction lt " +
           "WHERE lt.accountId = :accountId AND lt.orderId = :orderId " +
           "AND lt.transactionType = :transactionType AND lt.referenceType = :referenceType")
    BigDecimal sumAmountByAccountIdAndOrderIdAndTransactionTypeAndReferenceType(
            @Param("accountId") UUID accountId,
            @Param("orderId") UUID orderId,
            @Param("transactionType") TransactionType transactionType,
            @Param("referenceType") ReferenceType referenceType);
}
