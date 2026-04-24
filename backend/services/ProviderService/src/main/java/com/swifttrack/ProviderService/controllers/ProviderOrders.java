package com.swifttrack.ProviderService.controllers;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swifttrack.ProviderService.services.ProviderOrderServices;
import com.swifttrack.dto.Message;
import com.swifttrack.dto.orderDto.CreateOrderRequest;
import com.swifttrack.dto.orderDto.CreateOrderResponse;
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

    @PostMapping(value = "/v1/getQuote", consumes = "application/json")
    public ResponseEntity<QuoteResponse> getQuote(@RequestHeader String token, @RequestParam String providerCode,
            @RequestBody QuoteInput quoteInput) {
        return ResponseEntity.ok(providerOrderServices.getQuote(token, providerCode, quoteInput));
    }

    @PostMapping(value = "/v1/internal/getQuote", consumes = "application/json")
    public ResponseEntity<QuoteResponse> getQuoteInternal(@RequestParam String providerCode,
            @RequestBody QuoteInput quoteInput) {
        return ResponseEntity.ok(providerOrderServices.getQuoteInternal(providerCode, quoteInput));
    }

    @PostMapping(value = "/v1/createOrder", consumes = "application/json")
    public ResponseEntity<CreateOrderResponse> createOrder(@RequestHeader String token,
            @RequestParam UUID quoteSessionId, @RequestBody CreateOrderRequest createOrderRequest) {
        return ResponseEntity.ok(providerOrderServices.createOrder(token, quoteSessionId, createOrderRequest));
    }

    @PostMapping(value = "/v1/internal/createOrder", consumes = "application/json")
    public ResponseEntity<CreateOrderResponse> createOrderInternal(@RequestParam String providerCode,
            @RequestBody CreateOrderRequest createOrderRequest) {
        return ResponseEntity.ok(providerOrderServices.createOrderInternal(providerCode, createOrderRequest));
    }

    @PostMapping("/v1/cancelOrder")
    public ResponseEntity<Message> cancelOrder(@RequestHeader String token, @RequestParam String orderId,
            @RequestParam String providerCode) {
        return ResponseEntity.ok(providerOrderServices.cancelOrder(token, orderId, providerCode));
    }
}
