package com.swifttrack.AdminService.clients;

import com.swifttrack.enums.BillingAndSettlement.AccountType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "billingandsettlementservice", url = "http://localhost:8080/billingandsettlementservice")
public interface BillingClient {

    @GetMapping("/api/accounts/v1/getMyAccount")
    ResponseEntity<?> getAccountsByUserId(
            @RequestHeader("token") String token,
            @RequestParam UUID userId);

    @GetMapping("/api/accounts/v1/getTransactions")
    ResponseEntity<?> getTransactions(
            @RequestHeader("token") String token,
            @RequestParam UUID accountId);

    @PostMapping("/api/accounts/v1/admin/topupWallet")
    ResponseEntity<?> topUpWalletByAdmin(
            @RequestHeader("token") String token,
            @RequestParam UUID userId,
            @RequestParam BigDecimal amount);

    @PostMapping("/api/accounts/v1/admin")
    ResponseEntity<?> createAccountByAdmin(
            @RequestHeader("token") String token,
            @RequestParam UUID userId,
            @RequestParam AccountType accountType);

    @PostMapping("/api/accounts/v1/reconcile")
    ResponseEntity<String> reconcileBalance(
            @RequestHeader("token") String token,
            @RequestParam UUID accountId);

    @GetMapping("/api/settlements/pending")
    ResponseEntity<List<?>> getPendingSettlements();

    @GetMapping("/api/settlements/{settlementId}/transactions")
    ResponseEntity<List<?>> getSettlementTransactions(@PathVariable UUID settlementId);

    @PutMapping("/api/settlements/{settlementId}/settled")
    ResponseEntity<?> markSettled(
            @PathVariable UUID settlementId,
            @RequestParam(required = false) String externalReference);

    @PutMapping("/api/settlements/{settlementId}/failed")
    ResponseEntity<?> markFailed(
            @RequestHeader("token") String token,
            @PathVariable UUID settlementId);

    @GetMapping("/api/settlements/account/{accountId}")
    ResponseEntity<List<?>> getSettlementsByAccount(@PathVariable UUID accountId);

    @PostMapping("/api/margin-config")
    ResponseEntity<?> createMarginConfig(
            @RequestHeader("token") String token,
            @RequestBody Object request);

    @PutMapping("/api/margin-config/{configId}")
    ResponseEntity<?> updateMarginConfig(
            @RequestHeader("token") String token,
            @PathVariable UUID configId,
            @RequestBody Object request);

    @GetMapping("/api/margin-config/platform")
    ResponseEntity<?> getPlatformMarginConfigs(@RequestParam(required = false) String marginType);

    @GetMapping("/api/margin-config/user/{userId}")
    ResponseEntity<?> getUserMarginConfig(
            @PathVariable UUID userId,
            @RequestParam(required = false) String marginType);

    @PatchMapping("/api/margin-config/{configId}/inactive")
    ResponseEntity<String> deactivateMarginConfig(
            @RequestHeader("token") String token,
            @PathVariable UUID configId);
}
