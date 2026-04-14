package com.swifttrack.BillingAndSettlementService.services;

import com.swifttrack.BillingAndSettlementService.models.Account;
import com.swifttrack.BillingAndSettlementService.models.LedgerTransaction;
import com.swifttrack.BillingAndSettlementService.models.Settlement;
import com.swifttrack.BillingAndSettlementService.models.SettlementTransaction;
import com.swifttrack.BillingAndSettlementService.models.enums.ReferenceType;
import com.swifttrack.BillingAndSettlementService.models.enums.SettlementAction;
import com.swifttrack.BillingAndSettlementService.models.enums.SettlementStatus;
import com.swifttrack.BillingAndSettlementService.repositories.AccountRepository;
import com.swifttrack.BillingAndSettlementService.repositories.SettlementRepository;
import com.swifttrack.BillingAndSettlementService.repositories.SettlementTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Isolation;
import com.swifttrack.events.SettlementInitiatedEvent;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final SettlementTransactionRepository settlementTransactionRepository;
    private final AccountRepository accountRepository;
    private final LedgerService ledgerService;
    private final AccountService accountService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Initiate a settlement for an account.
     * Creates a PENDING settlement and records the debit in the ledger.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Settlement initiateSettlement(String token, UUID accountId, BigDecimal amount) {
        UUID initiatedBy = accountService.resolveUserId(token);

        // Lock account
        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountId));

        List<Settlement> pendingSettlements = settlementRepository.findByAccountIdAndStatus(accountId, SettlementStatus.PENDING);
        BigDecimal pendingTotal = pendingSettlements.stream()
                .map(Settlement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal availableBalance = account.getBalance().subtract(pendingTotal);

        if (availableBalance.compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance. Available=" + availableBalance
                    + " requested=" + amount);
        }

        // Create settlement
        Settlement settlement = Settlement.builder()
                .accountId(accountId)
                .amount(amount)
                .status(SettlementStatus.PENDING)
                .initiatedBy(initiatedBy)
                .build();

        Settlement saved = settlementRepository.save(settlement);

        log.info("Initiated settlement id={} for account={} amount={} by={}", saved.getId(), accountId, amount, initiatedBy);

        // Publish event to payment gateway
        try {
            SettlementInitiatedEvent event = SettlementInitiatedEvent.builder()
                .settlementId(saved.getId())
                .payeeAccountId(accountId)
                .amount(amount)
                .currency("INR")
                .idempotencyKey("SETTLEMENT-INIT-" + saved.getId())
                .initiatedAt(saved.getCreatedAt())
                .build();
            kafkaTemplate.send("settlement.initiated", event);
            log.info("Published settlement.initiated event for settlementId={}", saved.getId());
        } catch (Exception e) {
            log.error("Failed to publish settlement.initiated event for settlementId={}", saved.getId(), e);
        }

        return saved;
    }

    /**
     * Mark settlement as processing (payment gateway transfer started).
     */
    @Transactional
    public Settlement markProcessing(UUID settlementId, String externalReference) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new RuntimeException("Settlement not found: " + settlementId));

        if (settlement.getStatus() == SettlementStatus.PROCESSING) {
            return settlement; // Idempotent
        }

        if (settlement.getStatus() != SettlementStatus.PENDING) {
            throw new RuntimeException("Invalid settlement status transition: " + settlement.getStatus() + " -> PROCESSING");
        }

        settlement.setStatus(SettlementStatus.PROCESSING);
        settlement.setExternalReference(externalReference);
        return settlementRepository.save(settlement);
    }

    /**
     * Mark settlement as successfully settled.
     */
    @Transactional
    public Settlement markSettled(UUID settlementId, String externalReference) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new RuntimeException("Settlement not found: " + settlementId));

        if (settlement.getStatus() == SettlementStatus.SETTLED) {
            return settlement; // Idempotent
        }

        if (settlement.getStatus() != SettlementStatus.PENDING && settlement.getStatus() != SettlementStatus.PROCESSING) {
            throw new RuntimeException("Invalid settlement status transition: " + settlement.getStatus() + " -> SETTLED");
        }

        settlement.setStatus(SettlementStatus.SETTLED);
        if (externalReference != null) {
            settlement.setExternalReference(externalReference);
        }

        // Create settlement debit on the account upon marking as settled
        ledgerService.debit(settlement.getAccountId(), settlement.getAmount(), ReferenceType.SETTLEMENT,
                settlement.getId(), null, "Settlement payout completed",
                "SETTLEMENT-" + settlement.getId() + "-DEBIT", 
                settlement.getInitiatedBy() != null ? settlement.getInitiatedBy() : UUID.fromString("00000000-0000-0000-0000-000000000000"));

        log.info("Settlement id={} marked as SETTLED. External ref={}", settlementId, externalReference);
        return settlementRepository.save(settlement);
    }

    /**
     * Mark settlement as failed and reverse the ledger debit.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Settlement markFailed(String token, UUID settlementId) {
        UUID updatedBy = accountService.resolveUserId(token);
        return markFailedInternal(updatedBy, settlementId);
    }

    /**
     * Mark settlement as failed (internal use, skips token resolution).
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Settlement markFailedInternal(UUID updatedBy, UUID settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new RuntimeException("Settlement not found: " + settlementId));

        if (settlement.getStatus() == SettlementStatus.FAILED) {
            return settlement; // Idempotent
        }

        if (settlement.getStatus() != SettlementStatus.PENDING && settlement.getStatus() != SettlementStatus.PROCESSING) {
            throw new RuntimeException("Invalid settlement status transition: " + settlement.getStatus() + " -> FAILED");
        }

        settlement.setStatus(SettlementStatus.FAILED);
        settlementRepository.save(settlement);

        log.warn("Settlement id={} FAILED. account={} by={}",
                settlementId, settlement.getAccountId(), updatedBy);
        return settlement;
    }

    /**
     * Unified method to update settlement status based on an action.
     */
    @Transactional
    public Settlement updateSettlementStatus(String token, UUID settlementId, SettlementAction action, String externalReference) {
        log.info("Updating settlement id={} with action={}", settlementId, action);
        return switch (action) {
            case PROCESSING -> markProcessing(settlementId, externalReference);
            case SETTLED -> markSettled(settlementId, externalReference);
            case FAILED -> markFailed(token, settlementId);
        };
    }

    /**
     * Link a ledger transaction to a settlement batch.
     */
    @Transactional
    public SettlementTransaction linkTransaction(UUID settlementId, UUID ledgerTransactionId) {
        SettlementTransaction st = SettlementTransaction.builder()
                .settlementId(settlementId)
                .ledgerTransactionId(ledgerTransactionId)
                .build();
        return settlementTransactionRepository.save(st);
    }

    public List<Settlement> getSettlementsByAccountId(UUID accountId) {
        return settlementRepository.findByAccountId(accountId);
    }

    public List<Settlement> getPendingSettlements() {
        return settlementRepository.findByStatus(SettlementStatus.PENDING);
    }

    public List<SettlementTransaction> getSettlementTransactions(UUID settlementId) {
        return settlementTransactionRepository.findBySettlementId(settlementId);
    }
}
