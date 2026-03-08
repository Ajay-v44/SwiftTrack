package com.swifttrack.BillingAndSettlementService.controllers;

import com.swifttrack.BillingAndSettlementService.models.Settlement;
import com.swifttrack.BillingAndSettlementService.models.SettlementTransaction;
import com.swifttrack.BillingAndSettlementService.services.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping("/initiate")
    public ResponseEntity<Settlement> initiateSettlement(@RequestHeader("token") String token,
                                                          @RequestParam UUID accountId,
                                                          @RequestParam BigDecimal amount) {
        Settlement settlement = settlementService.initiateSettlement(token, accountId, amount);
        return ResponseEntity.ok(settlement);
    }

    @PutMapping("/{settlementId}/processing")
    public ResponseEntity<Settlement> markProcessing(@PathVariable UUID settlementId,
                                                      @RequestParam String externalReference) {
        Settlement settlement = settlementService.markProcessing(settlementId, externalReference);
        return ResponseEntity.ok(settlement);
    }

    @PutMapping("/{settlementId}/settled")
    public ResponseEntity<Settlement> markSettled(@PathVariable UUID settlementId,
                                                   @RequestParam(required = false) String externalReference) {
        Settlement settlement = settlementService.markSettled(settlementId, externalReference);
        return ResponseEntity.ok(settlement);
    }

    @PutMapping("/{settlementId}/failed")
    public ResponseEntity<Settlement> markFailed(@RequestHeader("token") String token,
                                                  @PathVariable UUID settlementId) {
        Settlement settlement = settlementService.markFailed(token, settlementId);
        return ResponseEntity.ok(settlement);
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<Settlement>> getSettlementsByAccount(@PathVariable UUID accountId) {
        return ResponseEntity.ok(settlementService.getSettlementsByAccountId(accountId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Settlement>> getPendingSettlements() {
        return ResponseEntity.ok(settlementService.getPendingSettlements());
    }

    @GetMapping("/{settlementId}/transactions")
    public ResponseEntity<List<SettlementTransaction>> getSettlementTransactions(@PathVariable UUID settlementId) {
        return ResponseEntity.ok(settlementService.getSettlementTransactions(settlementId));
    }
}
