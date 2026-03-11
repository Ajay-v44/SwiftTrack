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
    public PricingSnapshot createSnapshot(UUID quoteSessionId, BigDecimal providerCost, BigDecimal driverCost,
            BigDecimal platformMargin, BigDecimal tenantCharge,
            PricingSource pricingSource) {
        if (quoteSessionId == null) {
            throw new RuntimeException("quoteSessionId is required to create pricing snapshot");
        }
        Optional<PricingSnapshot> existing = pricingSnapshotRepository.findByQuoteSessionId(quoteSessionId);
        if (existing.isPresent()) {
            existing.get().setProviderCost(providerCost);
            existing.get().setDriverCost(driverCost);
            existing.get().setPlatformMargin(platformMargin);
            existing.get().setTenantCharge(tenantCharge);
            existing.get().setPricingSource(pricingSource);
            PricingSnapshot saved = pricingSnapshotRepository.save(existing.get());
            log.info("Updated pricing snapshot id={} for quoteSessionId={} source={} tenantCharge={} margin={}",
                    saved.getId(), quoteSessionId, pricingSource, tenantCharge, platformMargin);
            return saved;
        }

        PricingSnapshot snapshot = PricingSnapshot.builder()
                .quoteSessionId(quoteSessionId)
                .providerCost(providerCost)
                .driverCost(driverCost)
                .platformMargin(platformMargin)
                .tenantCharge(tenantCharge)
                .pricingSource(pricingSource)
                .build();

        PricingSnapshot saved = pricingSnapshotRepository.save(snapshot);
        log.info("Created pricing snapshot id={} for quoteSessionId={} source={} tenantCharge={} margin={}",
                saved.getId(), quoteSessionId, pricingSource, tenantCharge, platformMargin);
        return saved;
    }

    public Optional<PricingSnapshot> getByOrderId(UUID orderId) {
        return pricingSnapshotRepository.findByOrderId(orderId);
    }

    public Optional<PricingSnapshot> getByQuoteSessionId(UUID quoteSessionId) {
        return pricingSnapshotRepository.findByQuoteSessionId(quoteSessionId);
    }

    @Transactional
    public PricingSnapshot bindOrder(UUID quoteSessionId, UUID orderId) {
        PricingSnapshot snapshot = pricingSnapshotRepository.findByQuoteSessionId(quoteSessionId)
                .orElseThrow(() -> new RuntimeException("Pricing snapshot not found for quoteSessionId: " + quoteSessionId));
        if (snapshot.getOrderId() != null && !snapshot.getOrderId().equals(orderId)) {
            throw new RuntimeException("quoteSessionId is already bound to a different order");
        }
        snapshot.setOrderId(orderId);
        return pricingSnapshotRepository.save(snapshot);
    }
}
