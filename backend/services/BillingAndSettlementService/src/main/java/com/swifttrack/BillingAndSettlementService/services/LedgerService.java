package com.swifttrack.BillingAndSettlementService.services;

import com.swifttrack.BillingAndSettlementService.models.Account;
import com.swifttrack.BillingAndSettlementService.models.LedgerTransaction;
import com.swifttrack.BillingAndSettlementService.models.enums.AccountType;
import com.swifttrack.BillingAndSettlementService.models.enums.ReferenceType;
import com.swifttrack.BillingAndSettlementService.models.enums.TransactionType;
import com.swifttrack.BillingAndSettlementService.repositories.AccountRepository;
import com.swifttrack.BillingAndSettlementService.repositories.LedgerTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerService {

    private final LedgerTransactionRepository ledgerTransactionRepository;
    private final AccountRepository accountRepository;

    /**
     * Records a DEBIT transaction against an account.
     * Debit reduces the account balance (tenant owes money).
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public LedgerTransaction debit(UUID accountId, BigDecimal amount, ReferenceType referenceType,
                                    UUID referenceId, UUID orderId, String description,
                                    String idempotencyKey, UUID createdBy) {
        return recordTransaction(accountId, TransactionType.DEBIT, amount, referenceType,
                referenceId, orderId, description, idempotencyKey, createdBy);
    }

    /**
     * Records a CREDIT transaction against an account.
     * Credit increases the account balance (money owed to provider/driver/platform).
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public LedgerTransaction credit(UUID accountId, BigDecimal amount, ReferenceType referenceType,
                                     UUID referenceId, UUID orderId, String description,
                                     String idempotencyKey, UUID createdBy) {
        return recordTransaction(accountId, TransactionType.CREDIT, amount, referenceType,
                referenceId, orderId, description, idempotencyKey, createdBy);
    }

    /**
     * Core ledger entry creation with idempotency and balance update.
     */
    private LedgerTransaction recordTransaction(UUID accountId, TransactionType transactionType,
                                                 BigDecimal amount, ReferenceType referenceType,
                                                 UUID referenceId, UUID orderId, String description,
                                                 String idempotencyKey, UUID createdBy) {
        // Idempotency check
        if (idempotencyKey != null) {
            Optional<LedgerTransaction> existing = ledgerTransactionRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.warn("Duplicate transaction detected for idempotencyKey={}. Returning existing.", idempotencyKey);
                return existing.get();
            }
        }

        // Lock account for update
        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountId));

        if (!account.getIsActive()) {
            throw new RuntimeException("Account is inactive: " + accountId);
        }

        // Create ledger entry
        LedgerTransaction transaction = LedgerTransaction.builder()
                .accountId(accountId)
                .transactionType(transactionType)
                .amount(amount)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .orderId(orderId)
                .description(description)
                .idempotencyKey(idempotencyKey)
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .build();

        LedgerTransaction saved = ledgerTransactionRepository.save(transaction);

        // Update account balance
        if (transactionType == TransactionType.CREDIT) {
            account.setBalance(account.getBalance().add(amount));
        } else {
            account.setBalance(account.getBalance().subtract(amount));
        }
        account.setUpdatedBy(createdBy);
        accountRepository.save(account);

        log.info("Recorded {} {} on account {} | amount={} | ref={}:{} | order={}",
                transactionType, saved.getId(), accountId, amount, referenceType, referenceId, orderId);

        return saved;
    }

    public List<LedgerTransaction> getTransactionsByAccountId(UUID accountId) {
        return ledgerTransactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    public List<LedgerTransaction> getTransactionsByOrderId(UUID orderId) {
        return ledgerTransactionRepository.findByOrderId(orderId);
    }

    public BigDecimal calculateBalance(UUID accountId) {
        return ledgerTransactionRepository.calculateBalanceByAccountId(accountId);
    }
}
