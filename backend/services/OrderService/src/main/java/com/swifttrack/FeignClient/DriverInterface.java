package com.swifttrack.FeignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.swifttrack.OrderService.dto.AssignNearestDriversRequest;
import com.swifttrack.OrderService.dto.AssignNearestDriversResponse;

@FeignClient(name = "driverservice", url = "http://localhost:8080/driverservice")
public interface DriverInterface {

    @PostMapping("/drivers/assign-nearest")
    ResponseEntity<AssignNearestDriversResponse> assignNearestDrivers(
            @RequestHeader("token") String token,
            @RequestBody AssignNearestDriversRequest request);

    @PostMapping("/drivers/assign-nearest/internal")
    ResponseEntity<AssignNearestDriversResponse> assignNearestDriversInternal(
            @RequestBody AssignNearestDriversRequest request);
}
