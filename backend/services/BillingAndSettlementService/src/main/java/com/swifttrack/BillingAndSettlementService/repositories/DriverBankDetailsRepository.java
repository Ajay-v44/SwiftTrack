package com.swifttrack.BillingAndSettlementService.repositories;

import com.swifttrack.BillingAndSettlementService.models.DriverBankDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DriverBankDetailsRepository extends JpaRepository<DriverBankDetails, UUID> {
    Optional<DriverBankDetails> findByDriverId(UUID driverId);
}
