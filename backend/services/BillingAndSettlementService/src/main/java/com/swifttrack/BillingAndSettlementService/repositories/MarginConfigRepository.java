package com.swifttrack.BillingAndSettlementService.repositories;

import com.swifttrack.BillingAndSettlementService.models.MarginConfig;
import com.swifttrack.BillingAndSettlementService.models.enums.OrganizationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MarginConfigRepository extends JpaRepository<MarginConfig, UUID> {

    List<MarginConfig> findByUserIdAndIsActiveTrue(UUID userId);

    List<MarginConfig> findByUserIdAndOrganizationTypeAndIsActiveTrue(UUID userId, OrganizationType organizationType);

    List<MarginConfig> findByOrganizationTypeAndIsActiveTrue(OrganizationType organizationType);
}
