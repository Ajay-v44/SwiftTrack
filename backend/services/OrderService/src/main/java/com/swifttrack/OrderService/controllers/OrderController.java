package com.swifttrack.OrderService.controllers;

import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.swifttrack.OrderService.dto.AddressCreateOrderRequest;
import com.swifttrack.OrderService.dto.AddressQuoteRequest;
import com.swifttrack.OrderService.dto.PaginatedTenantOrdersResponse;
import com.swifttrack.OrderService.dto.TenantDeliveryAnalyticsDto;
import com.swifttrack.OrderService.dto.TenantDashboardSummaryDto;
import com.swifttrack.OrderService.services.OrderServices;
import com.swifttrack.dto.Message;
import com.swifttrack.dto.orderDto.CreateOrderRequest;
import com.swifttrack.dto.orderDto.DeliveryOptionsQuoteResponse;
import com.swifttrack.dto.orderDto.FinalCreateOrderResponse;
import com.swifttrack.dto.orderDto.GetOrdersForDriver;
import com.swifttrack.dto.orderDto.OrderQuoteResponse;
import com.swifttrack.dto.orderDto.OrderTrackingTimelineResponse;
import com.swifttrack.dto.providerDto.QuoteInput;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/order")
@Tag(name = "Order", description = "Order service")
public class OrderController {

    private OrderServices orderServices;

    public OrderController(OrderServices orderServices) {
        this.orderServices = orderServices;
    }

    @PostMapping("/v1/getQuote")
    public ResponseEntity<OrderQuoteResponse> getQuote(@RequestHeader("token") String token,
            @RequestBody AddressQuoteRequest quoteInput) {
        return ResponseEntity.ok(orderServices.getQuote(token, quoteInput));
    }

    @PostMapping("/v1/createOrder")
    public ResponseEntity<FinalCreateOrderResponse> createOrder(@RequestHeader("token") String token,
            @RequestParam("quoteSessionId") UUID quoteSessionId,
            @RequestBody AddressCreateOrderRequest createOrderRequest) {
        return ResponseEntity.ok(orderServices.createOrder(token, quoteSessionId, createOrderRequest));
    }

    @PostMapping("/v1/consumer/getQuote")
    public ResponseEntity<DeliveryOptionsQuoteResponse> getConsumerQuote(@RequestHeader("token") String token,
            @RequestBody AddressQuoteRequest quoteInput) {
        return ResponseEntity.ok(orderServices.getConsumerQuote(token, quoteInput));
    }

    @PostMapping("/v1/consumer/createOrder")
    public ResponseEntity<FinalCreateOrderResponse> createConsumerOrder(@RequestHeader("token") String token,
            @RequestParam("quoteSessionId") UUID quoteSessionId,
            @RequestParam("selectedQuoteId") UUID selectedQuoteId,
            @RequestBody AddressCreateOrderRequest createOrderRequest) {
        return ResponseEntity.ok(orderServices.createConsumerOrder(token, quoteSessionId, selectedQuoteId, createOrderRequest));
    }

    @PostMapping("/v1/guest/getQuote")
    public ResponseEntity<DeliveryOptionsQuoteResponse> getGuestQuote(@RequestBody QuoteInput quoteInput) {
        return ResponseEntity.ok(orderServices.getGuestQuote(quoteInput));
    }

    @PostMapping("/v1/guest/createOrder")
    public ResponseEntity<FinalCreateOrderResponse> createGuestOrder(@RequestParam("quoteSessionId") UUID quoteSessionId,
            @RequestParam("guestAccessToken") String guestAccessToken,
            @RequestParam("selectedQuoteId") UUID selectedQuoteId,
            @RequestBody CreateOrderRequest createOrderRequest) {
        return ResponseEntity.ok(orderServices.createGuestOrder(quoteSessionId, guestAccessToken, selectedQuoteId, createOrderRequest));
    }

    @PostMapping("/v1/cancelOrder")
    public ResponseEntity<Message> cancelOrder(@RequestHeader("token") String token,
            @RequestParam("orderId") UUID orderId) {
        return ResponseEntity.ok(orderServices.cancelOrder(token, orderId));
    }

    @PostMapping("/v1/getOrdersForDriver")
    public ResponseEntity<List<GetOrdersForDriver>> getOrdersForDriver(@RequestHeader("token") String token,
            @RequestBody com.swifttrack.dto.orderDto.GetOrdersRequest request) {
        return ResponseEntity.ok(orderServices.getOrdersForDriver(token, request));
    }

    @GetMapping("/v1/getOrderStatus/{orderId}")
    public ResponseEntity<String> getOrderStatus(@RequestHeader("token") String token,
            @PathVariable("orderId") UUID orderId) {
        return ResponseEntity.ok(orderServices.getOrderStatus(token, orderId));
    }

    @GetMapping("/v1/guest/getOrderStatus/{orderId}")
    public ResponseEntity<String> getGuestOrderStatus(@PathVariable("orderId") UUID orderId,
            @RequestParam("guestAccessToken") String guestAccessToken) {
        return ResponseEntity.ok(orderServices.getGuestOrderStatus(orderId, guestAccessToken));
    }

    @GetMapping("/v1/getOrderById/{orderId}")
    public ResponseEntity<com.swifttrack.dto.orderDto.OrderDetailsResponse> getOrderById(
            @RequestHeader("token") String token,
            @PathVariable("orderId") UUID orderId) {
        return ResponseEntity.ok(orderServices.getOrderById(token, orderId));
    }

    @GetMapping("/v1/getOrderTracking/{orderId}")
    public ResponseEntity<OrderTrackingTimelineResponse> getOrderTracking(
            @RequestHeader("token") String token,
            @PathVariable("orderId") UUID orderId) {
        return ResponseEntity.ok(orderServices.getOrderTracking(token, orderId));
    }

    @GetMapping("/v1/tenant/dashboard")
    public ResponseEntity<TenantDashboardSummaryDto> getTenantDashboardSummary(
            @RequestHeader("token") String token) {
        return ResponseEntity.ok(orderServices.getTenantDashboardSummary(token));
    }

    @GetMapping("/v1/tenant/analytics/delivery-volume")
    public ResponseEntity<TenantDeliveryAnalyticsDto> getTenantDeliveryAnalytics(
            @RequestHeader("token") String token,
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate) {
        return ResponseEntity.ok(orderServices.getTenantDeliveryAnalytics(token, startDate, endDate));
    }

    @GetMapping("/v1/tenant/orders")
    public ResponseEntity<PaginatedTenantOrdersResponse> getTenantOrders(
            @RequestHeader("token") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        return ResponseEntity.ok(orderServices.getTenantOrders(token, null, startDate, endDate, PageRequest.of(page, size)));
    }

    @GetMapping("/v1/tenant/orders/search")
    public ResponseEntity<PaginatedTenantOrdersResponse> searchTenantOrders(
            @RequestHeader("token") String token,
            @RequestParam("query") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        return ResponseEntity.ok(orderServices.getTenantOrders(token, query, startDate, endDate, PageRequest.of(page, size)));
    }

    @GetMapping("/v1/guest/getOrderById/{orderId}")
    public ResponseEntity<com.swifttrack.dto.orderDto.OrderDetailsResponse> getGuestOrderById(
            @PathVariable("orderId") UUID orderId,
            @RequestParam("guestAccessToken") String guestAccessToken) {
        return ResponseEntity.ok(orderServices.getGuestOrderById(orderId, guestAccessToken));
    }

    @GetMapping("/v1/public/track/{trackingId}")
    public ResponseEntity<OrderTrackingTimelineResponse> getPublicTracking(
            @PathVariable("trackingId") String trackingId) {
        return ResponseEntity.ok(orderServices.getPublicTracking(trackingId));
    }
}
