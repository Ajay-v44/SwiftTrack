package com.swifttrack.BillingAndSettlementService.services;

import com.swifttrack.BillingAndSettlementService.models.Account;
import com.swifttrack.BillingAndSettlementService.models.enums.ReferenceType;
import com.swifttrack.BillingAndSettlementService.models.enums.AccountType;
import com.swifttrack.BillingAndSettlementService.repositories.AccountRepository;
import com.swifttrack.BillingAndSettlementService.repositories.LedgerTransactionRepository;
import com.swifttrack.FeignClient.AuthInterface;
import com.swifttrack.dto.TokenResponse;
import com.swifttrack.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swifttrack.enums.UserType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;
    private final AuthInterface authInterface;
    private final LedgerService ledgerService;

    /**
     * Resolves the authenticated user's UUID from their JWT token.
     */
    public UUID resolveUserId(String token) {
        TokenResponse tokenResponse = authInterface.getUserDetails(token).getBody();
        if (tokenResponse == null || tokenResponse.id() == null) {
            throw new RuntimeException("Invalid token or user not found");
        }
        return tokenResponse.id();
    }

    /**
     * Resolves full TokenResponse from JWT token.
     */
    public TokenResponse resolveTokenResponse(String token) {
        TokenResponse tokenResponse = authInterface.getUserDetails(token).getBody();
        if (tokenResponse == null || tokenResponse.id() == null) {
            throw new RuntimeException("Invalid token or user not found");
        }
        return tokenResponse;
    }

    @Transactional
    public Account createAccount(String token, UUID userId, AccountType accountType) {
        UUID createdBy = resolveUserId(token);

        Optional<Account> existing = accountRepository.findByUserIdAndAccountType(userId, accountType);
        if (existing.isPresent()) {
            log.warn("Account already exists for userId={} accountType={}", userId, accountType);
            return existing.get();
        }

        Account account = Account.builder()
                .userId(userId)
                .accountType(accountType)
                .balance(BigDecimal.ZERO)
                .currency("INR")
                .isActive(true)
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .build();

        Account saved = accountRepository.save(account);
        log.info("Created ledger account id={} for userId={} type={} by={}", saved.getId(), userId, accountType,
                createdBy);
        return saved;
    }

    @Transactional
    public Account createAccountByAdmin(String token, UUID userId, AccountType accountType) {
        UUID createdBy = resolveUserId(token);

        // Add privilege check for account creation to prevent unauthorized creation
        // attacks
        verifyPrivilege(token, userId);

        Optional<Account> existing = accountRepository.findByUserIdAndAccountType(userId, accountType);
        if (existing.isPresent()) {
            log.warn("Account already exists for userId={} accountType={}", userId, accountType);
            return existing.get();
        }

        Account account = Account.builder()
                .userId(userId)
                .accountType(accountType)
                .balance(BigDecimal.ZERO)
                .currency("INR")
                .isActive(true)
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .build();

        Account saved = accountRepository.save(account);
        log.info("Created ledger account id={} for userId={} type={} by={}", saved.getId(), userId, accountType,
                createdBy);
        return saved;
    }

    /**
     * Internal account creation (used by other services with already-resolved
     * userId).
     */
    @Transactional
    public Account createAccountInternal(UUID userId, AccountType accountType, UUID createdBy) {
        Optional<Account> existing = accountRepository.findByUserIdAndAccountType(userId, accountType);
        if (existing.isPresent()) {
            return existing.get();
        }

        Account account = Account.builder()
                .userId(userId)
                .accountType(accountType)
                .balance(BigDecimal.ZERO)
                .currency("INR")
                .isActive(true)
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .build();

        return accountRepository.save(account);
    }

    public Optional<Account> getAccount(UUID accountId) {
        return accountRepository.findById(accountId);
    }

    public Optional<Account> getAccountByUserIdAndType(UUID userId, AccountType accountType) {
        return accountRepository.findByUserIdAndAccountType(userId, accountType);
    }

    public void verifyPrivilege(String token, UUID targetUserId) {
        TokenResponse tokenResponse = resolveTokenResponse(token);
        if (tokenResponse.id().equals(targetUserId)) {
            return;
        }

        boolean hasPrivilege = false;
        if (tokenResponse.userType().isPresent()) {
            UserType type = tokenResponse.userType().get();
            if (type == UserType.SUPER_ADMIN ||
                    type == UserType.TENANT_ADMIN ||
                    type == UserType.TENANT_USER ||
                    type == UserType.SYSTEM_ADMIN ||
                    type == UserType.SYSTEM_USER) {
                hasPrivilege = true;
            }
        }
        if (!hasPrivilege) {
            throw new RuntimeException("Unauthorized: Insufficient privileges to access this account");
        }
    }

    public void verifyAccountAccess(String token, UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountId));
        verifyPrivilege(token, account.getUserId());
    }

    public Account getAccountsByUserId(String token, UUID userId) {
        verifyPrivilege(token, userId);
        return accountRepository.findByUserId(userId);
    }

    public List<Account> getAccountsByType(AccountType accountType) {
        return accountRepository.findByAccountType(accountType);
    }

    @Transactional
    public Account topUpWalletByAdmin(String token, UUID userId, BigDecimal amount) {
        TokenResponse tokenResponse = resolveTokenResponse(token);
        if (tokenResponse.userType().isEmpty())
            throw new CustomException(HttpStatus.FORBIDDEN, "Unauthorized");
        if (tokenResponse.userType().get() != UserType.SUPER_ADMIN
                && tokenResponse.userType().get() != UserType.SYSTEM_USER)
            throw new CustomException(HttpStatus.FORBIDDEN, "Only SUPER_ADMIN or SYSTEM_USER can top up wallet");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new CustomException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero");

        Account account = accountRepository.findByUserId(userId);
        if (account == null || !account.getIsActive())
            throw new CustomException(HttpStatus.NOT_FOUND, "User account not found or inactive");

        ledgerService.credit(
                account.getId(),
                amount,
                ReferenceType.WALLET_TOPUP,
                UUID.randomUUID(),
                null,
                "Admin wallet top-up",
                null,
                tokenResponse.id());

        return accountRepository.findById(account.getId()).orElse(account);
    }

    /**
     * Reconcile account balance by recalculating from ledger transactions.
     * Returns true if the balance was correct, false if it was updated.
     */
    @Transactional
    public boolean reconcileBalance(UUID accountId) {
        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountId));

        BigDecimal calculatedBalance = ledgerTransactionRepository.calculateBalanceByAccountId(accountId);

        if (account.getBalance().compareTo(calculatedBalance) != 0) {
            log.warn("Balance mismatch for account {}. Stored={}, Calculated={}. Correcting.",
                    accountId, account.getBalance(), calculatedBalance);
            account.setBalance(calculatedBalance);
            accountRepository.save(account);
            return false;
        }

        log.info("Balance reconciliation passed for account {}. Balance={}", accountId, calculatedBalance);
        return true;
    }
}
