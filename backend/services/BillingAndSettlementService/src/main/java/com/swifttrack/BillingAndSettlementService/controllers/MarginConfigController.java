package com.swifttrack.BillingAndSettlementService.controllers;

import com.swifttrack.BillingAndSettlementService.dto.MarginConfigRequest;
import com.swifttrack.BillingAndSettlementService.dto.MarginConfigResponse;
import com.swifttrack.BillingAndSettlementService.dto.UserMarginConfigResponse;
import com.swifttrack.BillingAndSettlementService.models.enums.MarginType;
import com.swifttrack.BillingAndSettlementService.services.MarginConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/margin-config")
@RequiredArgsConstructor
public class MarginConfigController {

    private final MarginConfigService marginConfigService;

    @PostMapping
    public ResponseEntity<MarginConfigResponse> createConfig(@RequestHeader("token") String token,
            @RequestBody MarginConfigRequest request) {
        MarginConfigResponse config = marginConfigService.createConfig(token, request);
        return ResponseEntity.ok(config);
    }

    @PutMapping("/{configId}")
    public ResponseEntity<MarginConfigResponse> updateConfig(@RequestHeader("token") String token,
            @PathVariable UUID configId,
            @RequestBody MarginConfigRequest request) {
        MarginConfigResponse config = marginConfigService.updateConfig(token, configId, request);
        return ResponseEntity.ok(config);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserMarginConfigResponse> getActiveConfigByUser(@PathVariable UUID userId,
            @RequestParam(required = false) MarginType marginType) {
        return ResponseEntity.ok(marginConfigService.getActiveConfigByUserId(userId, marginType));
    }

    @GetMapping("/platform")
    public ResponseEntity<MarginConfigResponse> getPlatformConfigs(
            @RequestParam(required = false) MarginType marginType) {
        return ResponseEntity.ok(marginConfigService.getPlatformConfigs(marginType));
    }

    @PatchMapping("/{configId}/inactive")
    public ResponseEntity<String> deactivateConfig(@RequestHeader("token") String token,
            @PathVariable UUID configId) {
        marginConfigService.deactivateConfig(token, configId);
        return ResponseEntity.ok("Config deactivated");
    }
}
