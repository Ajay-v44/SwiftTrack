package com.swifttrack.AdminService.controllers;

import com.swifttrack.AdminService.clients.AIDispatchClient;
import com.swifttrack.AdminService.security.AdminGuard;
import com.swifttrack.AdminService.services.AuditService;
import com.swifttrack.dto.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Section: AI Dispatch Management
 * Admin APIs to monitor, test, and control the AI dispatch system.
 */
@RestController
@RequestMapping("/api/admin/ai-dispatch")
@Tag(name = "Admin - AI Dispatch",
        description = "Monitor and control AI-based driver dispatch. Health checks, manual dispatch triggers, and configuration. Admin-only.")
@RequiredArgsConstructor
public class AdminAIDispatchController {

    private final AdminGuard adminGuard;
    private final AIDispatchClient aiDispatchClient;
    private final AuditService auditService;

    @GetMapping("/v1/health")
    @Operation(summary = "AI Dispatch Service health check",
            description = "Check if the AI Dispatch Service (LLM inference pipeline) is running and healthy.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service is healthy"),
            @ApiResponse(responseCode = "503", description = "Service is down"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<String> checkDispatchHealth(@RequestHeader String token) {
        adminGuard.requireAdmin(token);
        return aiDispatchClient.health();
    }

    @PostMapping("/v1/manual-dispatch")
    @Operation(summary = "Manually trigger AI dispatch",
            description = "Force invoke the AI dispatch pipeline with a custom set of candidate drivers. " +
                    "Used for testing, overrides, and diagnostics. SUPER_ADMIN only.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dispatch completed"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Requires SUPER_ADMIN")
    })
    public ResponseEntity<?> manualDispatch(
            @RequestHeader String token,
            @RequestBody Object dispatchRequest) {
        TokenResponse admin = adminGuard.requireSuperAdmin(token);
        ResponseEntity<?> response = aiDispatchClient.assignDriver(dispatchRequest);

        auditService.log(admin, "AI_DISPATCH_MANUAL", "AI_DISPATCH", null, "DISPATCH",
                "Admin triggered manual AI dispatch");

        return response;
    }
}
