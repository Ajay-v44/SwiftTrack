package com.swifttrack.ProviderService.repositories;

import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swifttrack.ProviderService.models.TenantProviderRequest;

@Repository
public interface TenantProviderRequestRepository extends JpaRepository<TenantProviderRequest, UUID> {
    
    List<TenantProviderRequest> findByTenantId(UUID tenantId);
    
    List<TenantProviderRequest> findByStatus(String status);
    
    List<TenantProviderRequest> findByTenantIdAndStatus(UUID tenantId, String status);
}