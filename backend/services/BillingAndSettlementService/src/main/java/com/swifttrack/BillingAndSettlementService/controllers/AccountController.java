package com.swifttrack.BillingAndSettlementService.controllers;

import com.swifttrack.BillingAndSettlementService.dto.FinanceSummaryResponse;
import com.swifttrack.BillingAndSettlementService.dto.PaginatedLedgerTransactionsResponse;
import com.swifttrack.BillingAndSettlementService.dto.TodayExpenseResponse;
import com.swifttrack.BillingAndSettlementService.models.Account;
import com.swifttrack.BillingAndSettlementService.models.enums.AccountType;
import com.swifttrack.BillingAndSettlementService.services.AccountService;
import com.swifttrack.dto.billingDto.OrderDebitSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

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

    @PostMapping("/v1/internal/createAccount")
    public ResponseEntity<Account> createAccountInternal(@RequestParam UUID userId,
            @RequestParam AccountType accountType,
            @RequestParam UUID createdBy) {
        Account account = accountService.createAccountInternal(userId, accountType, createdBy);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/v1/getMyAccount")
    public ResponseEntity<Account> getAccountsByUserId(@RequestHeader("token") String token,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) AccountType accountType) {
        return ResponseEntity.ok(accountService.getAccountsByUserId(token, userId, accountType));
    }

    @GetMapping("/v1/getTransactions")
    public ResponseEntity<PaginatedLedgerTransactionsResponse> getTransactions(
            @RequestHeader("token") String token,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(accountService.getTransactions(token, accountId, page, size));
    }

    @GetMapping("/v1/dashboard/recent-expenses/today")
    public ResponseEntity<TodayExpenseResponse> getTodayExpenses(@RequestHeader("token") String token) {
        return ResponseEntity.ok(new TodayExpenseResponse(accountService.getTodayExpenses(token)));
    }

    @GetMapping("/v1/dashboard/summary")
    public ResponseEntity<FinanceSummaryResponse> getFinanceSummary(@RequestHeader("token") String token) {
        return ResponseEntity.ok(accountService.getFinanceSummary(token));
    }

    @GetMapping("/v1/internal/orderDebit")
    public ResponseEntity<OrderDebitSummaryResponse> getOrderDebitSummaryInternal(
            @RequestParam UUID accountId,
            @RequestParam UUID orderId) {
        return ResponseEntity.ok(accountService.getOrderDebitSummaryInternal(accountId, orderId));
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
