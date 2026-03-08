package com.swifttrack.BillingAndSettlementService.controllers;

import com.swifttrack.BillingAndSettlementService.models.LedgerTransaction;
import com.swifttrack.BillingAndSettlementService.models.PricingSnapshot;
import com.swifttrack.BillingAndSettlementService.services.BillingService;
import com.swifttrack.BillingAndSettlementService.services.LedgerService;
import com.swifttrack.BillingAndSettlementService.services.PricingSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;
    private final LedgerService ledgerService;
    private final PricingSnapshotService pricingSnapshotService;

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("BillingAndSettlementService is running");
    }

    @PostMapping("/process/external-provider")
    public ResponseEntity<PricingSnapshot> processExternalProvider(
            @RequestHeader("token") String token,
            @RequestParam UUID orderId,
            @RequestParam UUID tenantId,
            @RequestParam UUID providerId,
            @RequestParam BigDecimal providerCost,
            @RequestParam BigDecimal platformMargin) {
        PricingSnapshot snapshot = billingService.processExternalProviderOrder(
                token, orderId, tenantId, providerId, providerCost, platformMargin);
        return ResponseEntity.ok(snapshot);
    }

    @PostMapping("/process/tenant-driver")
    public ResponseEntity<PricingSnapshot> processTenantDriver(
            @RequestHeader("token") String token,
            @RequestParam UUID orderId,
            @RequestParam UUID tenantId,
            @RequestParam UUID driverId,
            @RequestParam BigDecimal driverCost,
            @RequestParam BigDecimal platformMargin) {
        PricingSnapshot snapshot = billingService.processTenantDriverOrder(
                token, orderId, tenantId, driverId, driverCost, platformMargin);
        return ResponseEntity.ok(snapshot);
    }

    @PostMapping("/process/gig-driver")
    public ResponseEntity<PricingSnapshot> processGigDriver(
            @RequestHeader("token") String token,
            @RequestParam UUID orderId,
            @RequestParam UUID tenantId,
            @RequestParam UUID driverId,
            @RequestParam BigDecimal driverEarning,
            @RequestParam BigDecimal platformCommission) {
        PricingSnapshot snapshot = billingService.processGigDriverOrder(
                token, orderId, tenantId, driverId, driverEarning, platformCommission);
        return ResponseEntity.ok(snapshot);
    }

    @GetMapping("/pricing/{orderId}")
    public ResponseEntity<PricingSnapshot> getPricingSnapshot(@PathVariable UUID orderId) {
        return pricingSnapshotService.getByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ledger/order/{orderId}")
    public ResponseEntity<List<LedgerTransaction>> getOrderTransactions(@PathVariable UUID orderId) {
        return ResponseEntity.ok(ledgerService.getTransactionsByOrderId(orderId));
    }
}
