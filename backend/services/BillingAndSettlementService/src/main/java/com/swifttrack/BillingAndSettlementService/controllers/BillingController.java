package com.swifttrack.BillingAndSettlementService.controllers;

import com.swifttrack.BillingAndSettlementService.dto.QuoteRequest;
import com.swifttrack.BillingAndSettlementService.dto.QuoteResponse;
import com.swifttrack.BillingAndSettlementService.services.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PostMapping("/getQuote")
    public ResponseEntity<QuoteResponse> getQuote(@RequestBody QuoteRequest request) {
        return ResponseEntity.ok(billingService.getQuote(request));
    }
}
