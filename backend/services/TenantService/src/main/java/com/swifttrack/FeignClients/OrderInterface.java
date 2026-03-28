package com.swifttrack.FeignClients;

import java.time.LocalDate;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.swifttrack.dto.TenantDeliveryAnalyticsDto;

@FeignClient(name = "orderservice")
public interface OrderInterface {

    @GetMapping("/api/order/v1/tenant/analytics/delivery-volume")
    ResponseEntity<TenantDeliveryAnalyticsDto> getTenantDeliveryAnalytics(
            @RequestHeader("token") String token,
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate);
}
