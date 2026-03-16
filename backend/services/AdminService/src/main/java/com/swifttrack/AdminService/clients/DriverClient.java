package com.swifttrack.AdminService.clients;

import com.swifttrack.dto.Message;
import com.swifttrack.dto.driverDto.GetAllDriverUser;
import com.swifttrack.dto.driverDto.GetTenantDrivers;
import com.swifttrack.enums.VerificationStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "driverservice", url = "http://localhost:8080/driverservice")
public interface DriverClient {

    @PostMapping("/api/driver/v1/getDriverUsers")
    ResponseEntity<List<GetAllDriverUser>> getDriverUsers(
            @RequestParam String token,
            @RequestParam VerificationStatus verificationStatus);

    @PostMapping("/api/driver/v1/acceptDriver")
    ResponseEntity<Message> acceptDriver(
            @RequestHeader String token,
            @RequestParam UUID driverId);

    @PostMapping("/api/driver/v1/getTenantDrivers")
    ResponseEntity<List<GetTenantDrivers>> getTenantDrivers(
            @RequestHeader String token);

    @GetMapping("/api/driver/v1/status/{driverId}")
    ResponseEntity<?> getDriverStatus(@PathVariable UUID driverId);

    @GetMapping("/api/driver/v1/location/{driverId}")
    ResponseEntity<?> getDriverLocation(@PathVariable UUID driverId);

    @GetMapping("/api/driver/v1/available/{driverId}")
    ResponseEntity<Boolean> isDriverAvailable(@PathVariable UUID driverId);

    @PostMapping("/api/driver/v1/internal/cancelAssignedOrder")
    ResponseEntity<Message> cancelAssignedOrderInternal(
            @RequestParam UUID orderId,
            @RequestParam(required = false) String reason);
}
