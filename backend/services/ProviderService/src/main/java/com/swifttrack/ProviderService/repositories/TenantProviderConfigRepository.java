package com.swifttrack.ProviderService.repositories;

import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swifttrack.ProviderService.models.TenantProviderConfig;

@Repository
public interface TenantProviderConfigRepository extends JpaRepository<TenantProviderConfig, UUID> {
    
    List<TenantProviderConfig> findByTenantId(UUID tenantId);
    
    List<TenantProviderConfig> findByProviderId(UUID providerId);
    
    TenantProviderConfig findByTenantIdAndProviderId(UUID tenantId, UUID providerId);
}