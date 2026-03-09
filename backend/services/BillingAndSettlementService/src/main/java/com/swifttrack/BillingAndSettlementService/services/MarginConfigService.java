package com.swifttrack.BillingAndSettlementService.services;

import com.swifttrack.BillingAndSettlementService.dto.MarginConfigRequest;
import com.swifttrack.BillingAndSettlementService.dto.MarginConfigResponse;
import com.swifttrack.BillingAndSettlementService.dto.UserMarginConfigResponse;
import com.swifttrack.BillingAndSettlementService.models.MarginConfig;
import com.swifttrack.BillingAndSettlementService.models.enums.MarginType;
import com.swifttrack.BillingAndSettlementService.models.enums.OrganizationType;
import com.swifttrack.BillingAndSettlementService.repositories.MarginConfigRepository;
import com.swifttrack.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarginConfigService {

    private final MarginConfigRepository marginConfigRepository;
    private final AccountService accountService;

    @Transactional
    public MarginConfigResponse createConfig(String token, MarginConfigRequest request) {
        UUID createdBy = accountService.resolveUserId(token);
        validateRequest(request);

        MarginConfig config = MarginConfig.builder()
                .userId(request.getUserId())
                .organizationType(request.getOrganizationType())
                .marginType(request.getMarginType())
                .key(request.getKey())
                .value(request.getValue())
                .baseFare(request.getBaseFare())
                .perKmRate(request.getPerKmRate())
                .commissionPercent(request.getCommissionPercent())
                .minimumPlatformFee(request.getMinimumPlatformFee())
                .isActive(true)
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .build();

        MarginConfig saved = marginConfigRepository.save(config);
        log.info("Created margin config id={} for userId={} type={} key={} value={} by={}",
                saved.getId(), request.getUserId(), request.getMarginType(), request.getKey(), request.getValue(),
                createdBy);
        return toResponse(saved);
    }

    @Transactional
    public MarginConfigResponse updateConfig(String token, UUID configId, MarginConfigRequest request) {
        UUID updatedBy = accountService.resolveUserId(token);
        validateRequest(request);

        MarginConfig config = marginConfigRepository.findById(configId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Margin config not found"));

        config.setUserId(request.getUserId());
        config.setOrganizationType(request.getOrganizationType());
        config.setMarginType(request.getMarginType());
        config.setKey(request.getKey());
        config.setValue(request.getValue());
        config.setBaseFare(request.getBaseFare());
        config.setPerKmRate(request.getPerKmRate());
        config.setCommissionPercent(request.getCommissionPercent());
        config.setMinimumPlatformFee(request.getMinimumPlatformFee());
        config.setUpdatedBy(updatedBy);

        MarginConfig saved = marginConfigRepository.save(config);
        log.info("Updated margin config id={} marginType={} by={}", configId, request.getMarginType(), updatedBy);
        return toResponse(saved);
    }

    public UserMarginConfigResponse getActiveConfigByUserId(UUID userId, MarginType marginType) {
        MarginConfig config = marginConfigRepository
                .findFirstByUserIdAndMarginTypeAndIsActiveTrueOrderByUpdatedAtDesc(userId, marginType)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "No active margin config found for user"));
        return toUserResponse(config);
    }

    public MarginConfigResponse getPlatformConfigs(MarginType marginType) {
        MarginConfig config = marginConfigRepository.findByOrganizationTypeAndMarginTypeAndIsActiveTrue(
                OrganizationType.SWIFTTRACK, marginType)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Platform margin config not found"));
        return toResponse(config);
    }

    @Transactional
    public void deactivateConfig(String token, UUID configId) {
        UUID updatedBy = accountService.resolveUserId(token);

        MarginConfig config = marginConfigRepository.findById(configId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "Margin config not found"));
        config.setIsActive(false);
        config.setUpdatedBy(updatedBy);
        marginConfigRepository.save(config);
        log.info("Deactivated margin config id={} by={}", configId, updatedBy);
    }

    private MarginConfigResponse toResponse(MarginConfig config) {
        return MarginConfigResponse.builder()
                .key(config.getKey())
                .value(config.getValue())
                .minimumPlatformFee(config.getMinimumPlatformFee())
                .baseFare(config.getBaseFare())
                .perKmRate(config.getPerKmRate())
                .commissionPercent(config.getCommissionPercent())
                .build();
    }

    private UserMarginConfigResponse toUserResponse(MarginConfig config) {
        return UserMarginConfigResponse.builder()
                .key(config.getKey())
                .value(config.getValue())
                .minimumPlatformFee(config.getMinimumPlatformFee())
                .marginType(config.getMarginType())
                .baseFare(config.getBaseFare())
                .perKmRate(config.getPerKmRate())
                .commissionPercent(config.getCommissionPercent())
                .build();
    }

    private void validateRequest(MarginConfigRequest request) {
        if (request == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Request body is required");
        }
        if (request.getUserId() == null
                || request.getOrganizationType() == null
                || request.getMarginType() == null
                || request.getKey() == null
                || request.getKey().isBlank()
                || request.getValue() == null
                || request.getMinimumPlatformFee() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST,
                    "userId, organizationType, marginType, key, value and minimumPlatformFee are required");
        }

        if (request.getMarginType() == MarginType.DISTANCE_RATE
                && (Objects.isNull(request.getBaseFare()) || Objects.isNull(request.getPerKmRate()))) {
            throw new CustomException(HttpStatus.BAD_REQUEST,
                    "baseFare and perKmRate are required for DISTANCE_RATE margin type");
        }

        if (request.getMarginType() == MarginType.ORDER_RATE && Objects.isNull(request.getCommissionPercent())) {
            throw new CustomException(HttpStatus.BAD_REQUEST,
                    "commissionPercent is required for ORDER_RATE margin type");
        }
    }
}
