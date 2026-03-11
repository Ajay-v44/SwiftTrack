package com.swifttrack.BillingAndSettlementService.services;

import com.swifttrack.BillingAndSettlementService.dto.MarginConfigResponse;
import com.swifttrack.BillingAndSettlementService.dto.QuoteRequest;
import com.swifttrack.BillingAndSettlementService.dto.QuoteResponse;
import com.swifttrack.BillingAndSettlementService.dto.UserMarginConfigResponse;
import com.swifttrack.BillingAndSettlementService.models.enums.MarginType;
import com.swifttrack.BillingAndSettlementService.models.enums.PricingSource;
import com.swifttrack.BillingAndSettlementService.models.enums.SelectedType;
import com.swifttrack.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillingService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final MarginConfigService marginConfigService;
    private final PricingSnapshotService pricingSnapshotService;

    @Transactional
    public QuoteResponse getQuote(QuoteRequest request) {
        validateRequest(request);
        SelectedType selectedType = SelectedType.fromValue(request.getSelectedType());

        return switch (selectedType) {
            case TENANT_DRIVERS ->
                getTenantDriverQuote(request.getUserId(), request.getDistance().get(), request.getQuoteSessionId());
            case LOCAL_DRIVERS -> getLocalDriverQuote(request.getDistance().get(), request.getQuoteSessionId());
            case EXTERNAL_PROVIDERS -> getExternalProviderQuote(request.getPrice().get(), request.getQuoteSessionId());
        };
    }

    @Transactional
    public void bindOrder(UUID quoteSessionId, UUID orderId) {
        if (quoteSessionId == null || orderId == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "quoteSessionId and orderId are required");
        }
        pricingSnapshotService.bindOrder(quoteSessionId, orderId);
    }

    private QuoteResponse getTenantDriverQuote(UUID userId, BigDecimal distance, UUID quoteSessionId) {
        UserMarginConfigResponse marginConfig = marginConfigService.getActiveConfigByUserId(userId,
                MarginType.DISTANCE_RATE);
        BigDecimal safeDistance = toNonNegative(distance);

        BigDecimal baseFare = toNonNegative(marginConfig.getBaseFare());
        BigDecimal perKmRate = toNonNegative(marginConfig.getPerKmRate());
        BigDecimal commissionPercent = toNonNegative(marginConfig.getCommissionPercent());

        BigDecimal driverCost = baseFare.add(safeDistance.multiply(perKmRate));
        BigDecimal platformMargin = percentageOf(driverCost, commissionPercent);
        BigDecimal minimumPlatformFee = toNonNegative(marginConfig.getMinimumPlatformFee());

        platformMargin = platformMargin.compareTo(minimumPlatformFee) < 0
                ? minimumPlatformFee
                : platformMargin;
        BigDecimal tenantCharge = driverCost.add(platformMargin);

        pricingSnapshotService.createSnapshot(
                quoteSessionId,
                bdZero(),
                money(driverCost),
                money(platformMargin),
                money(tenantCharge),
                PricingSource.TENANT_DRIVER);

        return QuoteResponse.builder()
                .driverCost(money(driverCost))
                .platformMargin(money(platformMargin))
                .providerCost(bdZero())
                .tenantCharge(money(tenantCharge))
                .build();
    }

    private QuoteResponse getLocalDriverQuote(BigDecimal distance, UUID quoteSessionId) {
        MarginConfigResponse marginConfig = marginConfigService.getPlatformConfigs(MarginType.DISTANCE_RATE);
        BigDecimal safeDistance = toNonNegative(distance);

        BigDecimal baseFare = toNonNegative(marginConfig.getBaseFare());
        BigDecimal perKmRate = toNonNegative(marginConfig.getPerKmRate());
        BigDecimal commissionPercent = toNonNegative(marginConfig.getCommissionPercent());

        BigDecimal driverCost = baseFare.add(safeDistance.multiply(perKmRate));
        BigDecimal platformMargin = percentageOf(driverCost, commissionPercent);
        BigDecimal minimumPlatformFee = toNonNegative(marginConfig.getMinimumPlatformFee());

        platformMargin = platformMargin.compareTo(minimumPlatformFee) < 0
                ? minimumPlatformFee
                : platformMargin;
        BigDecimal tenantCharge = driverCost.add(platformMargin);

        pricingSnapshotService.createSnapshot(
                quoteSessionId,
                bdZero(),
                money(driverCost),
                money(platformMargin),
                money(tenantCharge),
                PricingSource.GIG_DRIVER);

        return QuoteResponse.builder()
                .driverCost(money(driverCost))
                .platformMargin(money(platformMargin))
                .providerCost(bdZero())
                .tenantCharge(money(tenantCharge))
                .build();
    }

    private QuoteResponse getExternalProviderQuote(BigDecimal price, UUID quoteSessionId) {
        if (price == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "price is required for EXTERNAL_PROVIDERS");
        }

        MarginConfigResponse marginConfig = marginConfigService.getPlatformConfigs(MarginType.ORDER_RATE);
        BigDecimal providerCost = toNonNegative(price);

        BigDecimal commissionPercent = toNonNegative(marginConfig.getCommissionPercent());
        BigDecimal baseFare = toNonNegative(marginConfig.getBaseFare());
        BigDecimal minimumPlatformFee = toNonNegative(marginConfig.getMinimumPlatformFee());

        BigDecimal commission = percentageOf(providerCost, commissionPercent);
        BigDecimal commissionPlusBaseFare = commission.add(baseFare);

        BigDecimal platformMargin = commissionPlusBaseFare.compareTo(minimumPlatformFee) < 0
                ? minimumPlatformFee
                : commissionPlusBaseFare;
        BigDecimal tenantCharge = providerCost.add(platformMargin);

        pricingSnapshotService.createSnapshot(
                quoteSessionId,
                money(providerCost),
                bdZero(),
                money(platformMargin),
                money(tenantCharge),
                PricingSource.PROVIDER);

        return QuoteResponse.builder()
                .driverCost(bdZero())
                .platformMargin(money(platformMargin))
                .providerCost(money(providerCost))
                .tenantCharge(money(tenantCharge))
                .build();
    }

    private void validateRequest(QuoteRequest request) {
        if (request == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Request body is required");
        }
        if (request.getUserId() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        if (request.getQuoteSessionId() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "quoteSessionId is required");
        }
        if (request.getSelectedType() == null || request.getSelectedType().isBlank()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "selectedType is required");
        }
    }

    private BigDecimal percentageOf(BigDecimal amount, BigDecimal percent) {
        return amount.multiply(percent).divide(ONE_HUNDRED, 6, RoundingMode.HALF_UP);
    }

    private BigDecimal toNonNegative(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "negative values are not allowed");
        }
        return value;
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal bdZero() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
}
