package com.swifttrack.DriverService.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.swifttrack.DriverService.models.DriverVehicleDetails;

@Repository
public interface DriverVehicleDetailsRepository extends JpaRepository<DriverVehicleDetails, UUID> {
    Page<DriverVehicleDetails> findByTenantId(UUID tenantId, Pageable pageable);
}
