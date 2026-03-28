package com.swifttrack.controllers;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swifttrack.dto.TenantDeliveryAnalyticsDto;
import com.swifttrack.services.TenantDashboardService;

@RestController
@RequestMapping("/company/v1/dashboard")
public class TenantDashboardController {

    private final TenantDashboardService tenantDashboardService;

    public TenantDashboardController(TenantDashboardService tenantDashboardService) {
        this.tenantDashboardService = tenantDashboardService;
    }

    @GetMapping("/delivery-volume")
    public ResponseEntity<TenantDeliveryAnalyticsDto> getDeliveryAnalytics(
            @RequestHeader("token") String token,
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate) {
        return ResponseEntity.ok(tenantDashboardService.getDeliveryAnalytics(token, startDate, endDate));
    }
}
