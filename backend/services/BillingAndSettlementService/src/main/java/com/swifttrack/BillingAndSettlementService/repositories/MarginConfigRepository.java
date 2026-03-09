package com.swifttrack.BillingAndSettlementService.repositories;

import com.swifttrack.BillingAndSettlementService.models.MarginConfig;
import com.swifttrack.BillingAndSettlementService.models.enums.MarginType;
import com.swifttrack.BillingAndSettlementService.models.enums.OrganizationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MarginConfigRepository extends JpaRepository<MarginConfig, UUID> {

        Optional<MarginConfig> findFirstByUserIdAndIsActiveTrueOrderByUpdatedAtDesc(UUID userId);

        Optional<MarginConfig> findFirstByUserIdAndMarginTypeAndIsActiveTrueOrderByUpdatedAtDesc(UUID userId,
                        MarginType marginType);

        List<MarginConfig> findByOrganizationTypeAndIsActiveTrue(OrganizationType organizationType);

        Optional<MarginConfig> findByOrganizationTypeAndMarginTypeAndIsActiveTrue(OrganizationType organizationType,
                        MarginType marginType);
}
