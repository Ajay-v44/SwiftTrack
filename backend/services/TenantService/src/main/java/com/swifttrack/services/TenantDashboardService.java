package com.swifttrack.services;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.swifttrack.FeignClients.OrderInterface;
import com.swifttrack.dto.TenantDeliveryAnalyticsDto;

@Service
public class TenantDashboardService {

    private final OrderInterface orderInterface;

    public TenantDashboardService(OrderInterface orderInterface) {
        this.orderInterface = orderInterface;
    }

    public TenantDeliveryAnalyticsDto getDeliveryAnalytics(String token, LocalDate startDate, LocalDate endDate) {
        return orderInterface.getTenantDeliveryAnalytics(token, startDate, endDate).getBody();
    }
}
