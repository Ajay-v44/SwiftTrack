package com.swifttrack.AdminService.controllers;

import com.swifttrack.AdminService.clients.OrderClient;
import com.swifttrack.AdminService.dto.AdminCancelOrderRequest;
import com.swifttrack.AdminService.security.AdminGuard;
import com.swifttrack.AdminService.services.AuditService;
import com.swifttrack.dto.Message;
import com.swifttrack.dto.TokenResponse;
import com.swifttrack.dto.orderDto.OrderDetailsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Section: Order Management
 * Admin APIs to view order details, check statuses, and force-cancel orders.
 */
@RestController
@RequestMapping("/api/admin/orders")
@Tag(name = "Admin - Order Management",
        description = "Order monitoring and control. View details, check statuses, and force-cancel problematic orders. Admin-only.")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminGuard adminGuard;
    private final OrderClient orderClient;
    private final AuditService auditService;

    @GetMapping("/v1/{orderId}")
    @Operation(summary = "Get order details by ID",
            description = "Retrieve full order details for any order in the system.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order details returned"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderDetailsResponse> getOrderById(
            @RequestHeader String token,
            @PathVariable UUID orderId) {
        adminGuard.requireAdmin(token);
        return orderClient.getOrderById(token, orderId);
    }

    @GetMapping("/v1/{orderId}/status")
    @Operation(summary = "Get order status",
            description = "Get the current status of an order. Useful for monitoring stuck or delayed orders.")
    public ResponseEntity<String> getOrderStatus(
            @RequestHeader String token,
            @PathVariable UUID orderId) {
        adminGuard.requireAdmin(token);
        return orderClient.getOrderStatus(token, orderId);
    }

    @PostMapping("/v1/cancel")
    @Operation(summary = "Force-cancel an order",
            description = "Admin force-cancel of any order, regardless of state. Use for stuck, problematic, or fraudulent orders. Reason is mandatory.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order cancelled"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<Message> cancelOrder(
            @RequestHeader String token,
            @RequestBody AdminCancelOrderRequest request) {
        TokenResponse admin = adminGuard.requireAdmin(token);
        ResponseEntity<Message> response = orderClient.cancelOrder(token, request.orderId());

        auditService.log(admin, "ORDER_CANCEL", "ORDER", request.orderId(), "ORDER",
                "Admin force-cancelled order: " + request.orderId() + ", reason: " + request.reason());

        return response;
    }
}
