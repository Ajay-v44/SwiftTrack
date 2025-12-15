package com.swifttrack.ProviderService.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swifttrack.ProviderService.services.ProviderOrderServices;
import com.swifttrack.dto.providerDto.QuoteInput;
import com.swifttrack.dto.providerDto.QuoteResponse;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/provider/orders")
@Tag(name = "Provider Orders", description = "Provider Orders")
public class ProviderOrders {

    ProviderOrderServices providerOrderServices;

    public ProviderOrders(ProviderOrderServices providerOrderServices) {
        this.providerOrderServices = providerOrderServices;
    }

    @PostMapping("/v1/getQuote")
    public ResponseEntity<QuoteResponse> getQuote(@RequestHeader String token, @RequestBody QuoteInput quoteInput) {
        return ResponseEntity.ok(providerOrderServices.getQuote(token, quoteInput));
    }
}
