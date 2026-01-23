package com.swifttrack.OrderService.controllers;

import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.swifttrack.OrderService.services.OrderServices;
import com.swifttrack.dto.Message;
import com.swifttrack.dto.orderDto.CreateOrderRequest;
import com.swifttrack.dto.orderDto.CreateOrderResponse;
import com.swifttrack.dto.orderDto.OrderQuoteResponse;
import com.swifttrack.dto.providerDto.QuoteInput;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/order")
@Tag(name = "Order", description = "Order service")
public class OrderController {

    private OrderServices orderServices;

    public OrderController(OrderServices orderServices) {
        this.orderServices = orderServices;
    }

    @PostMapping("/v1/getQuote")
    public ResponseEntity<OrderQuoteResponse> getQuote(@RequestHeader String token,
            @RequestBody QuoteInput quoteInput) {
        return ResponseEntity.ok(orderServices.getQuote(token, quoteInput));
    }

    @PostMapping("/v1/createOrder")
    public ResponseEntity<CreateOrderResponse> createOrder(@RequestHeader String token,
            @RequestParam UUID quoteSessionId, @RequestBody CreateOrderRequest createOrderRequest) {
        return ResponseEntity.ok(orderServices.createOrder(token, quoteSessionId, createOrderRequest));
    }

    @PostMapping("/v1/cancelOrder")
    public ResponseEntity<Message> cancelOrder(@RequestHeader String token, @RequestParam String orderId,
            @RequestParam String providerCode) {
        return ResponseEntity.ok(orderServices.cancelOrder(token, orderId, providerCode));
    }

}
