package com.swifttrack.BillingAndSettlementService.services;

import com.swifttrack.BillingAndSettlementService.models.MarginConfig;
import com.swifttrack.BillingAndSettlementService.models.enums.MarginType;
import com.swifttrack.BillingAndSettlementService.models.enums.OrganizationType;
import com.swifttrack.BillingAndSettlementService.repositories.MarginConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarginConfigService {

    private final MarginConfigRepository marginConfigRepository;
    private final AccountService accountService;

    @Transactional
    public MarginConfig createOrUpdateConfig(String token, UUID userId, OrganizationType organizationType,
                                              MarginType marginType, String key, BigDecimal value) {
        UUID createdBy = accountService.resolveUserId(token);

        MarginConfig config = MarginConfig.builder()
                .userId(userId)
                .organizationType(organizationType)
                .marginType(marginType)
                .key(key)
                .value(value)
                .isActive(true)
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .build();

        MarginConfig saved = marginConfigRepository.save(config);
        log.info("Created margin config id={} for userId={} type={} key={} value={} by={}",
                saved.getId(), userId, marginType, key, value, createdBy);
        return saved;
    }

    public List<MarginConfig> getActiveConfigsByUserId(UUID userId) {
        return marginConfigRepository.findByUserIdAndIsActiveTrue(userId);
    }

    public List<MarginConfig> getActiveConfigsByUserIdAndOrgType(UUID userId, OrganizationType organizationType) {
        return marginConfigRepository.findByUserIdAndOrganizationTypeAndIsActiveTrue(userId, organizationType);
    }

    public List<MarginConfig> getPlatformConfigs() {
        return marginConfigRepository.findByOrganizationTypeAndIsActiveTrue(OrganizationType.SWIFTTRACK);
    }

    @Transactional
    public void deactivateConfig(String token, UUID configId) {
        UUID updatedBy = accountService.resolveUserId(token);

        Optional<MarginConfig> configOpt = marginConfigRepository.findById(configId);
        if (configOpt.isPresent()) {
            MarginConfig config = configOpt.get();
            config.setIsActive(false);
            config.setUpdatedBy(updatedBy);
            marginConfigRepository.save(config);
            log.info("Deactivated margin config id={} by={}", configId, updatedBy);
        }
    }
}
