package com.swifttrack.BillingAndSettlementService.controllers;

import com.swifttrack.BillingAndSettlementService.models.Account;
import com.swifttrack.BillingAndSettlementService.models.LedgerTransaction;
import com.swifttrack.BillingAndSettlementService.models.enums.AccountType;
import com.swifttrack.BillingAndSettlementService.services.AccountService;
import com.swifttrack.BillingAndSettlementService.services.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final LedgerService ledgerService;

    @PostMapping("/v1/createAccount")
    public ResponseEntity<Account> createAccount(@RequestHeader("token") String token,
            @RequestParam UUID userId,
            @RequestParam AccountType accountType) {
        Account account = accountService.createAccount(token, userId, accountType);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/v1/admin")
    public ResponseEntity<Account> createAccountByAdmin(@RequestHeader("token") String token,
            @RequestParam UUID userId,
            @RequestParam AccountType accountType) {
        Account account = accountService.createAccountByAdmin(token, userId, accountType);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/v1/getMyAccount")
    public ResponseEntity<Account> getAccountsByUserId(@RequestHeader("token") String token,
            @RequestParam UUID userId) {
        return ResponseEntity.ok(accountService.getAccountsByUserId(token, userId));
    }

    @GetMapping("/v1/getTransactions")
    public ResponseEntity<List<LedgerTransaction>> getTransactions(@RequestHeader("token") String token,
            @RequestParam UUID accountId) {
        accountService.verifyAccountAccess(token, accountId);
        return ResponseEntity.ok(ledgerService.getTransactionsByAccountId(accountId));
    }

    @PostMapping("/v1/reconcile")
    public ResponseEntity<String> reconcileBalance(@RequestHeader("token") String token, @RequestParam UUID accountId) {
        accountService.verifyAccountAccess(token, accountId);
        boolean correct = accountService.reconcileBalance(accountId);
        if (correct) {
            return ResponseEntity.ok("Balance is correct");
        }
        return ResponseEntity.ok("Balance was corrected from ledger transactions");
    }

    @PostMapping("/v1/admin/topupWallet")
    public ResponseEntity<Account> topUpWalletByAdmin(@RequestHeader("token") String token,
            @RequestParam UUID userId,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(accountService.topUpWalletByAdmin(token, userId, amount));
    }
}
