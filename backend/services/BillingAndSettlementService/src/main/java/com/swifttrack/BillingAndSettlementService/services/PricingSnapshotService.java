package com.swifttrack.BillingAndSettlementService.services;

import com.swifttrack.BillingAndSettlementService.models.PricingSnapshot;
import com.swifttrack.BillingAndSettlementService.models.enums.PricingSource;
import com.swifttrack.BillingAndSettlementService.repositories.PricingSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingSnapshotService {

    private final PricingSnapshotRepository pricingSnapshotRepository;

    @Transactional
    public PricingSnapshot createSnapshot(UUID orderId, BigDecimal providerCost, BigDecimal driverCost,
            BigDecimal platformMargin, BigDecimal tenantCharge,
            PricingSource pricingSource) {
        // Check if snapshot already exists for this order
        Optional<PricingSnapshot> existing = pricingSnapshotRepository.findByOrderId(orderId);
        if (existing.isPresent()) {
            existing.get().setProviderCost(providerCost);
            existing.get().setDriverCost(driverCost);
            existing.get().setPlatformMargin(platformMargin);
            existing.get().setTenantCharge(tenantCharge);
            existing.get().setPricingSource(pricingSource);
            PricingSnapshot saved = pricingSnapshotRepository.save(existing.get());
            log.info("Updated pricing snapshot id={} for orderId={} source={} tenantCharge={} margin={}",
                    saved.getId(), orderId, pricingSource, tenantCharge, platformMargin);
            return saved;
        }

        PricingSnapshot snapshot = PricingSnapshot.builder()
                .orderId(orderId)
                .providerCost(providerCost)
                .driverCost(driverCost)
                .platformMargin(platformMargin)
                .tenantCharge(tenantCharge)
                .pricingSource(pricingSource)
                .build();

        PricingSnapshot saved = pricingSnapshotRepository.save(snapshot);
        log.info("Created pricing snapshot id={} for orderId={} source={} tenantCharge={} margin={}",
                saved.getId(), orderId, pricingSource, tenantCharge, platformMargin);
        return saved;
    }

    public Optional<PricingSnapshot> getByOrderId(UUID orderId) {
        return pricingSnapshotRepository.findByOrderId(orderId);
    }
}
