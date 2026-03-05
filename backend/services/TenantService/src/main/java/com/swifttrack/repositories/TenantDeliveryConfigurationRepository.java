package com.swifttrack.repositories;

import com.swifttrack.Models.TenantDeliveryConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TenantDeliveryConfigurationRepository extends JpaRepository<TenantDeliveryConfiguration, UUID> {

    void deleteByTenantId(UUID tenantId);
}
