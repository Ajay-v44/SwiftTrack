package com.swifttrack.FeignClient;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.swifttrack.dto.orderDto.GetOrdersForDriver;
import com.swifttrack.dto.orderDto.GetOrdersRequest;

@FeignClient(name = "orderservice", url = "http://localhost:8080/orderservice")
public interface OrderInterface {

        @PostMapping("/api/order/v1/getOrdersForDriver")
        public ResponseEntity<List<GetOrdersForDriver>> getOrdersForDriver(@RequestHeader("token") String token,
                        @RequestBody GetOrdersRequest request);

        @GetMapping("/api/order/v1/getOrderStatus/{orderId}")
        public ResponseEntity<String> getOrderStatus(@RequestHeader("token") String token,
                        @PathVariable UUID orderId);
}
