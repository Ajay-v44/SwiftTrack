package com.swifttrack.FeignClient;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.swifttrack.dto.orderDto.OrderQuoteSessionResponse;

@FeignClient(name = "orderservice", url = "http://localhost:8080/orderservice/order-quote-session")
public interface OrderQuoteSessionInterface {

    @GetMapping("/v1/get-order-quote-session")
    List<OrderQuoteSessionResponse> getOrderQuoteSession(@RequestParam UUID quoteSessionId);
}
