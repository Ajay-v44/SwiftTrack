package com.swifttrack.OrderService.dto;

import java.util.List;
import java.util.UUID;

import com.swifttrack.dto.orderDto.CreateOrderRequest;
import com.swifttrack.enums.PaymentType;

public record AddressCreateOrderRequest(
        String idempotencyKey,
        String tenantId,
        String quoteId,
        String orderReference,
        CreateOrderRequest.OrderType orderType,
        PaymentType paymentType,
        UUID pickupAddressId,
        CreateOrderRequest.LocationPoint dropoff,
        List<CreateOrderRequest.Item> items,
        CreateOrderRequest.PackageInfo packageInfo,
        CreateOrderRequest.TimeWindows timeWindows,
        CreateOrderRequest.DeliveryPreferences deliveryPreferences,
        CreateOrderRequest.ExternalMetadata externalMetadata,
        String deliveryInstructions) {
}
