package com.swifttrack.FeignClient;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.swifttrack.dto.orderDto.GetOrdersForDriver;
import com.swifttrack.dto.orderDto.GetOrdersRequest;

@FeignClient(name = "OrderService")
public interface OrderInterface {

    @PostMapping("/v1/getOrdersForDriver")
    public ResponseEntity<List<GetOrdersForDriver>> getOrdersForDriver(@RequestHeader("token") String token,
            @RequestBody GetOrdersRequest request);
}
