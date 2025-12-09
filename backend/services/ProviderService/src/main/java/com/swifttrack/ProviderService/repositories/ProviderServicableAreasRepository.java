package com.swifttrack.ProviderService.repositories;

import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swifttrack.ProviderService.models.ProviderServicableAreas;

@Repository
public interface ProviderServicableAreasRepository extends JpaRepository<ProviderServicableAreas, UUID> {
    
    List<ProviderServicableAreas> findByProviderId(UUID providerId);
    
    List<ProviderServicableAreas> findByIsActive(boolean isActive);
    
    List<ProviderServicableAreas> findByProviderIdAndIsActive(UUID providerId, boolean isActive);
}