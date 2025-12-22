package com.swifttrack.OrderService.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swifttrack.OrderService.services.OrderQuoteSessionService;
import com.swifttrack.dto.orderDto.OrderQuoteSessionResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/order-quote-session")
@Tag(name = "Order Quote Session", description = "Order Quote Session Endpoints")
public class OrderQuoteSessionController {
    OrderQuoteSessionService orderQuoteSessionService;

    public OrderQuoteSessionController(OrderQuoteSessionService orderQuoteSessionService) {
        this.orderQuoteSessionService = orderQuoteSessionService;
    }

    @GetMapping("/v1/get-order-quote-session")
    @Operation(summary = "Get Order Quote Session")
    public ResponseEntity<List<OrderQuoteSessionResponse>> getOrderQuoteSession(@RequestParam UUID quoteSessionId) {
        return ResponseEntity.ok(orderQuoteSessionService.getOrderQuoteSession(quoteSessionId));
    }
}
