package com.swifttrack.AdminService.controllers;

import com.swifttrack.AdminService.clients.AIDispatchClient;
import com.swifttrack.AdminService.security.AdminGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Section: Platform Overview & Health
 * Admin dashboard data APIs: service health, platform stats overview.
 */
@RestController
@RequestMapping("/api/admin/platform")
@Tag(name = "Admin - Platform Overview",
        description = "Platform health monitoring and high-level stats. Use this to check service availability and system status.")
@RequiredArgsConstructor
public class AdminPlatformController {

    private final AdminGuard adminGuard;
    private final AIDispatchClient aiDispatchClient;

    @GetMapping("/v1/health")
    @Operation(summary = "Admin service self health check",
            description = "Confirm the AdminService itself is running correctly.")
    public ResponseEntity<Map<String, Object>> selfHealth() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("service", "AdminService");
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/v1/services/health")
    @Operation(summary = "Downstream service health checks",
            description = "Ping all major downstream services and return their health status. Admin-only.")
    public ResponseEntity<Map<String, Object>> servicesHealth(@RequestHeader String token) {
        adminGuard.requireAdmin(token);

        Map<String, Object> health = new LinkedHashMap<>();
        health.put("adminService", "UP");
        health.put("timestamp", LocalDateTime.now().toString());

        // Check AI Dispatch
        try {
            ResponseEntity<String> aiHealth = aiDispatchClient.health();
            health.put("aiDispatchService", aiHealth.getStatusCode().is2xxSuccessful() ? "UP" : "DEGRADED");
        } catch (Exception e) {
            health.put("aiDispatchService", "DOWN - " + e.getMessage());
        }

        return ResponseEntity.ok(health);
    }

    @GetMapping("/v1/info")
    @Operation(summary = "Platform info",
            description = "Basic platform metadata: version, environment, etc.")
    public ResponseEntity<Map<String, Object>> platformInfo(@RequestHeader String token) {
        adminGuard.requireAdmin(token);

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("platform", "SwiftTrack");
        info.put("version", "1.0.0");
        info.put("adminServiceVersion", "1.0.0");
        info.put("environment", System.getenv().getOrDefault("APP_ENV", "development"));
        info.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(info);
    }
}
