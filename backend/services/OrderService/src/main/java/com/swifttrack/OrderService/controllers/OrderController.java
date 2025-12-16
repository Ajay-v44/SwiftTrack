package com.swifttrack.OrderService.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import com.swifttrack.OrderService.services.OrderServices;
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
    public ResponseEntity<Object> getQuote(@RequestHeader String token, @RequestBody QuoteInput quoteInput) {
        return ResponseEntity.ok(orderServices.getQuote(token, quoteInput));
    }

}
