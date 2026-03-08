package com.swifttrack.BillingAndSettlementService.controllers;

import com.swifttrack.BillingAndSettlementService.models.MarginConfig;
import com.swifttrack.BillingAndSettlementService.models.enums.MarginType;
import com.swifttrack.BillingAndSettlementService.models.enums.OrganizationType;
import com.swifttrack.BillingAndSettlementService.services.MarginConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/margin-config")
@RequiredArgsConstructor
public class MarginConfigController {

    private final MarginConfigService marginConfigService;

    @PostMapping
    public ResponseEntity<MarginConfig> createConfig(@RequestHeader("token") String token,
                                                      @RequestParam UUID userId,
                                                      @RequestParam OrganizationType organizationType,
                                                      @RequestParam MarginType marginType,
                                                      @RequestParam String key,
                                                      @RequestParam BigDecimal value) {
        MarginConfig config = marginConfigService.createOrUpdateConfig(
                token, userId, organizationType, marginType, key, value);
        return ResponseEntity.ok(config);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MarginConfig>> getActiveConfigsByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(marginConfigService.getActiveConfigsByUserId(userId));
    }

    @GetMapping("/user/{userId}/type/{organizationType}")
    public ResponseEntity<List<MarginConfig>> getActiveConfigsByUserAndOrgType(
            @PathVariable UUID userId, @PathVariable OrganizationType organizationType) {
        return ResponseEntity.ok(marginConfigService.getActiveConfigsByUserIdAndOrgType(userId, organizationType));
    }

    @GetMapping("/platform")
    public ResponseEntity<List<MarginConfig>> getPlatformConfigs() {
        return ResponseEntity.ok(marginConfigService.getPlatformConfigs());
    }

    @DeleteMapping("/{configId}")
    public ResponseEntity<String> deactivateConfig(@RequestHeader("token") String token,
                                                     @PathVariable UUID configId) {
        marginConfigService.deactivateConfig(token, configId);
        return ResponseEntity.ok("Config deactivated");
    }
}
