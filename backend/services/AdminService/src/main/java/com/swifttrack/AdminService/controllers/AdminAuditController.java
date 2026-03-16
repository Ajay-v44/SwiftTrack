package com.swifttrack.AdminService.controllers;

import com.swifttrack.AdminService.models.AdminAuditLog;
import com.swifttrack.AdminService.security.AdminGuard;
import com.swifttrack.AdminService.services.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Section: Audit Logs
 * Admin APIs to query and review the complete audit trail of admin actions.
 */
@RestController
@RequestMapping("/api/admin/audit")
@Tag(name = "Admin - Audit Logs",
        description = "Query the complete audit trail of admin actions. Filter by admin, action type, service domain, target, or date range. SUPER_ADMIN only.")
@RequiredArgsConstructor
public class AdminAuditController {

    private final AdminGuard adminGuard;
    private final AuditService auditService;

    @GetMapping("/v1/logs")
    @Operation(summary = "Get all admin audit logs",
            description = "Paginated list of all admin actions across the platform. SUPER_ADMIN only.")
    @ApiResponse(responseCode = "200", description = "Audit logs returned")
    public ResponseEntity<Page<AdminAuditLog>> getAllAuditLogs(
            @RequestHeader String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        adminGuard.requireSuperAdmin(token);
        return ResponseEntity.ok(auditService.getAuditLogs(page, size));
    }

    @GetMapping("/v1/logs/admin/{adminId}")
    @Operation(summary = "Get audit logs by admin",
            description = "Filter audit logs for a specific admin user.")
    public ResponseEntity<Page<AdminAuditLog>> getAuditLogsByAdmin(
            @RequestHeader String token,
            @PathVariable UUID adminId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        adminGuard.requireSuperAdmin(token);
        return ResponseEntity.ok(auditService.getAuditLogsByAdmin(adminId, page, size));
    }

    @GetMapping("/v1/logs/action/{actionType}")
    @Operation(summary = "Get audit logs by action type",
            description = "Filter audit logs by specific action type e.g. DRIVER_APPROVE, ORDER_CANCEL, WALLET_TOPUP.")
    public ResponseEntity<Page<AdminAuditLog>> getAuditLogsByAction(
            @RequestHeader String token,
            @PathVariable String actionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        adminGuard.requireAdmin(token);
        return ResponseEntity.ok(auditService.getAuditLogsByActionType(actionType, page, size));
    }

    @GetMapping("/v1/logs/target/{targetId}")
    @Operation(summary = "Get audit logs for a target entity",
            description = "View the full admin action history for a specific user, driver, order, or other entity.")
    public ResponseEntity<Page<AdminAuditLog>> getAuditLogsByTarget(
            @RequestHeader String token,
            @PathVariable UUID targetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        adminGuard.requireAdmin(token);
        return ResponseEntity.ok(auditService.getAuditLogsByTarget(targetId, page, size));
    }

    @GetMapping("/v1/logs/domain/{domain}")
    @Operation(summary = "Get audit logs by service domain",
            description = "Filter audit logs by service area: USER, DRIVER, ORDER, BILLING, PROVIDER, AI_DISPATCH.")
    public ResponseEntity<Page<AdminAuditLog>> getAuditLogsByDomain(
            @RequestHeader String token,
            @PathVariable String domain,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        adminGuard.requireAdmin(token);
        return ResponseEntity.ok(auditService.getAuditLogsByDomain(domain, page, size));
    }

    @GetMapping("/v1/logs/dateRange")
    @Operation(summary = "Get audit logs within a date range",
            description = "Query audit logs within a specific time window. Format: yyyy-MM-ddTHH:mm:ss")
    public ResponseEntity<List<AdminAuditLog>> getAuditLogsByDateRange(
            @RequestHeader String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        adminGuard.requireSuperAdmin(token);
        return ResponseEntity.ok(auditService.getAuditLogsByDateRange(from, to));
    }
}
