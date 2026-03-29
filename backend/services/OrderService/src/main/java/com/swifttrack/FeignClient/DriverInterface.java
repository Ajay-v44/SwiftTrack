package com.swifttrack.FeignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.swifttrack.OrderService.dto.AssignNearestDriversRequest;
import com.swifttrack.OrderService.dto.AssignNearestDriversResponse;
import com.swifttrack.OrderService.dto.DriverLocationSnapshot;
import com.swifttrack.dto.Message;

@FeignClient(name = "driverservice", url = "http://localhost:8080/driverservice")
public interface DriverInterface {

    @PostMapping("/drivers/assign-nearest")
    ResponseEntity<AssignNearestDriversResponse> assignNearestDrivers(
            @RequestHeader("token") String token,
            @RequestBody AssignNearestDriversRequest request);

    @PostMapping("/drivers/assign-nearest/internal")
    ResponseEntity<AssignNearestDriversResponse> assignNearestDriversInternal(
            @RequestBody AssignNearestDriversRequest request);

    @PostMapping("/api/driver/v1/internal/cancelAssignedOrder")
    ResponseEntity<Message> cancelAssignedOrderInternal(
            @RequestParam("orderId") java.util.UUID orderId,
            @RequestParam(value = "reason", required = false) String reason);

    @GetMapping("/api/driver/v1/location/{driverId}")
    ResponseEntity<DriverLocationSnapshot> getDriverLocation(
            @org.springframework.web.bind.annotation.PathVariable("driverId") java.util.UUID driverId);
}
