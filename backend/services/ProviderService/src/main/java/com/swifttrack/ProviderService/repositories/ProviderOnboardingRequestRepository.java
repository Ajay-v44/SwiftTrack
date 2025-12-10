package com.swifttrack.ProviderService.repositories;

import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.swifttrack.ProviderService.models.enums.Status;

import com.swifttrack.ProviderService.models.ProviderOnboardingRequest;

@Repository
public interface ProviderOnboardingRequestRepository extends JpaRepository<ProviderOnboardingRequest, UUID> {
    
    ProviderOnboardingRequest findByRequestedUserId(UUID uuid);
    
    List<ProviderOnboardingRequest> findByStatus(Status status);
    
    List<ProviderOnboardingRequest> findByRequestedUserIdAndStatus(UUID uuid, Status status);
}