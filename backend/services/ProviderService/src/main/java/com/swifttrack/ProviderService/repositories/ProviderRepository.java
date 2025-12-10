package com.swifttrack.ProviderService.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swifttrack.ProviderService.models.Provider;

@Repository
public interface ProviderRepository extends JpaRepository<Provider,UUID> {

    List<Provider> findByIsActive(boolean isActive);
    Provider findByProviderName(String providerName);

}