package com.swifttrack.AdminService.clients;

import com.swifttrack.dto.Message;
import com.swifttrack.dto.orderDto.OrderDetailsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "orderservice", url = "http://localhost:8080/orderservice")
public interface OrderClient {

    @GetMapping("/api/order/v1/getOrderById/{orderId}")
    ResponseEntity<OrderDetailsResponse> getOrderById(
            @RequestHeader("token") String token,
            @PathVariable("orderId") UUID orderId);

    @GetMapping("/api/order/v1/getOrderStatus/{orderId}")
    ResponseEntity<String> getOrderStatus(
            @RequestHeader("token") String token,
            @PathVariable("orderId") UUID orderId);

    @PostMapping("/api/order/v1/cancelOrder")
    ResponseEntity<Message> cancelOrder(
            @RequestHeader("token") String token,
            @RequestParam("orderId") UUID orderId);
}
