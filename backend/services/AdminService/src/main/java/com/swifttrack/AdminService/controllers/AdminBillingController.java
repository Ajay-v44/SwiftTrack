package com.swifttrack.AdminService.controllers;

import com.swifttrack.AdminService.clients.BillingClient;
import com.swifttrack.AdminService.dto.AdminMarginConfigRequest;
import com.swifttrack.AdminService.dto.AdminWalletTopUpRequest;
import com.swifttrack.AdminService.security.AdminGuard;
import com.swifttrack.AdminService.services.AuditService;
import com.swifttrack.dto.TokenResponse;
import com.swifttrack.enums.BillingAndSettlement.AccountType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Section: Billing & Settlement Management
 * Admin APIs for account management, wallet top-ups, settlements, and margin configuration.
 */
@RestController
@RequestMapping("/api/admin/billing")
@Tag(name = "Admin - Billing & Settlement",
        description = "Full billing and financial management. Accounts, wallet top-ups, settlement processing, margin config. Admin-only.")
@RequiredArgsConstructor
public class AdminBillingController {

    private final AdminGuard adminGuard;
    private final BillingClient billingClient;
    private final AuditService auditService;

    // ========== Account Management ==========

    @GetMapping("/v1/accounts/user/{userId}")
    @Operation(summary = "Get user account", description = "Retrieve billing account details for any user.")
    public ResponseEntity<?> getUserAccount(
            @RequestHeader String token,
            @PathVariable UUID userId) {
        adminGuard.requireAdmin(token);
        return billingClient.getAccountsByUserId(token, userId);
    }

    @GetMapping("/v1/accounts/{accountId}/transactions")
    @Operation(summary = "Get account transactions",
            description = "Retrieve all ledger transactions for a specific account.")
    public ResponseEntity<?> getAccountTransactions(
            @RequestHeader String token,
            @PathVariable UUID accountId) {
        adminGuard.requireAdmin(token);
        return billingClient.getTransactions(token, accountId);
    }

    @PostMapping("/v1/accounts/create")
    @Operation(summary = "Create account for user (Admin)",
            description = "Manually create a billing account for a user. Use for account corrections.")
    public ResponseEntity<?> createAccountForUser(
            @RequestHeader String token,
            @RequestParam UUID userId,
            @RequestParam AccountType accountType) {
        TokenResponse admin = adminGuard.requireAdmin(token);
        ResponseEntity<?> response = billingClient.createAccountByAdmin(token, userId, accountType);

        auditService.log(admin, "ACCOUNT_CREATE", "BILLING", userId, "USER",
                "Admin created account type=" + accountType + " for user=" + userId);

        return response;
    }

    @PostMapping("/v1/accounts/{accountId}/reconcile")
    @Operation(summary = "Reconcile account balance",
            description = "Verify and correct account balance against ledger transactions.")
    public ResponseEntity<String> reconcileAccountBalance(
            @RequestHeader String token,
            @PathVariable UUID accountId) {
        TokenResponse admin = adminGuard.requireAdmin(token);
        ResponseEntity<String> response = billingClient.reconcileBalance(token, accountId);

        auditService.log(admin, "ACCOUNT_RECONCILE", "BILLING", accountId, "ACCOUNT",
                "Admin reconciled account: " + accountId);

        return response;
    }

    // ========== Wallet Management ==========

    @PostMapping("/v1/wallet/topup")
    @Operation(summary = "Top up user wallet",
            description = "Admin top-up of any user's wallet. Requires SUPER_ADMIN. Full audit trail recorded.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wallet topped up"),
            @ApiResponse(responseCode = "403", description = "Requires SUPER_ADMIN")
    })
    public ResponseEntity<?> topUpWallet(
            @RequestHeader String token,
            @RequestBody AdminWalletTopUpRequest request) {
        TokenResponse admin = adminGuard.requireSuperAdmin(token);
        ResponseEntity<?> response = billingClient.topUpWalletByAdmin(token, request.userId(), request.amount());

        auditService.log(admin, "WALLET_TOPUP", "BILLING", request.userId(), "USER",
                "Admin topped up wallet: userId=" + request.userId() +
                        ", amount=" + request.amount() + ", note=" + request.note());

        return response;
    }

    // ========== Settlement Management ==========

    @GetMapping("/v1/settlements/pending")
    @Operation(summary = "List pending settlements",
            description = "Get all settlements awaiting processing.")
    public ResponseEntity<?> getPendingSettlements(@RequestHeader String token) {
        adminGuard.requireAdmin(token);
        return billingClient.getPendingSettlements();
    }

    @GetMapping("/v1/settlements/account/{accountId}")
    @Operation(summary = "Get settlements by account",
            description = "View all settlement records for a specific billing account.")
    public ResponseEntity<?> getSettlementsByAccount(
            @RequestHeader String token,
            @PathVariable UUID accountId) {
        adminGuard.requireAdmin(token);
        return billingClient.getSettlementsByAccount(accountId);
    }

    @GetMapping("/v1/settlements/{settlementId}/transactions")
    @Operation(summary = "Get settlement transactions",
            description = "View the detailed transaction breakdown for a specific settlement.")
    public ResponseEntity<?> getSettlementTransactions(
            @RequestHeader String token,
            @PathVariable UUID settlementId) {
        adminGuard.requireAdmin(token);
        return billingClient.getSettlementTransactions(settlementId);
    }

    @PutMapping("/v1/settlements/{settlementId}/settle")
    @Operation(summary = "Mark settlement as settled",
            description = "Confirm a settlement has been completed. Provide external transfer reference if available.")
    public ResponseEntity<?> markSettlementSettled(
            @RequestHeader String token,
            @PathVariable UUID settlementId,
            @RequestParam(required = false) String externalReference) {
        TokenResponse admin = adminGuard.requireAdmin(token);
        ResponseEntity<?> response = billingClient.markSettled(settlementId, externalReference);

        auditService.log(admin, "SETTLEMENT_MARK_SETTLED", "BILLING", settlementId, "SETTLEMENT",
                "Admin marked settlement " + settlementId + " as settled. Ref=" + externalReference);

        return response;
    }

    @PutMapping("/v1/settlements/{settlementId}/fail")
    @Operation(summary = "Mark settlement as failed",
            description = "Mark a settlement as failed. This will trigger re-queuing if configured.")
    public ResponseEntity<?> markSettlementFailed(
            @RequestHeader String token,
            @PathVariable UUID settlementId) {
        TokenResponse admin = adminGuard.requireAdmin(token);
        ResponseEntity<?> response = billingClient.markFailed(token, settlementId);

        auditService.log(admin, "SETTLEMENT_MARK_FAILED", "BILLING", settlementId, "SETTLEMENT",
                "Admin marked settlement " + settlementId + " as FAILED.");

        return response;
    }

    // ========== Margin Configuration ==========

    @GetMapping("/v1/margin-config/platform")
    @Operation(summary = "Get platform margin configurations",
            description = "View the global margin configurations applied across the platform.")
    public ResponseEntity<?> getPlatformMarginConfig(
            @RequestHeader String token,
            @RequestParam(required = false) String marginType) {
        adminGuard.requireAdmin(token);
        return billingClient.getPlatformMarginConfigs(marginType);
    }

    @GetMapping("/v1/margin-config/user/{userId}")
    @Operation(summary = "Get user-specific margin config",
            description = "View the margin configuration applied for a specific user/tenant.")
    public ResponseEntity<?> getUserMarginConfig(
            @RequestHeader String token,
            @PathVariable UUID userId,
            @RequestParam(required = false) String marginType) {
        adminGuard.requireAdmin(token);
        return billingClient.getUserMarginConfig(userId, marginType);
    }

    @PostMapping("/v1/margin-config")
    @Operation(summary = "Create margin configuration",
            description = "Define a new margin/commission rule. SUPER_ADMIN only.")
    public ResponseEntity<?> createMarginConfig(
            @RequestHeader String token,
            @RequestBody AdminMarginConfigRequest request) {
        TokenResponse admin = adminGuard.requireSuperAdmin(token);
        ResponseEntity<?> response = billingClient.createMarginConfig(token, request);

        auditService.log(admin, "MARGIN_CONFIG_CREATE", "BILLING", request.userId(), "MARGIN_CONFIG",
                "Admin created margin config: type=" + request.marginType() + ", value=" + request.marginValue());

        return response;
    }

    @PutMapping("/v1/margin-config/{configId}")
    @Operation(summary = "Update margin configuration",
            description = "Modify an existing margin config. SUPER_ADMIN only.")
    public ResponseEntity<?> updateMarginConfig(
            @RequestHeader String token,
            @PathVariable UUID configId,
            @RequestBody AdminMarginConfigRequest request) {
        TokenResponse admin = adminGuard.requireSuperAdmin(token);
        ResponseEntity<?> response = billingClient.updateMarginConfig(token, configId, request);

        auditService.log(admin, "MARGIN_CONFIG_UPDATE", "BILLING", configId, "MARGIN_CONFIG",
                "Admin updated margin config: id=" + configId);

        return response;
    }

    @PatchMapping("/v1/margin-config/{configId}/deactivate")
    @Operation(summary = "Deactivate a margin config",
            description = "Deactivate an active margin configuration without deleting it.")
    public ResponseEntity<String> deactivateMarginConfig(
            @RequestHeader String token,
            @PathVariable UUID configId) {
        TokenResponse admin = adminGuard.requireSuperAdmin(token);
        ResponseEntity<String> response = billingClient.deactivateMarginConfig(token, configId);

        auditService.log(admin, "MARGIN_CONFIG_DEACTIVATE", "BILLING", configId, "MARGIN_CONFIG",
                "Admin deactivated margin config: " + configId);

        return response;
    }
}
