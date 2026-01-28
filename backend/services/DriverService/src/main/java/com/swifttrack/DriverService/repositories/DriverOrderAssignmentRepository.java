package com.swifttrack.DriverService.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swifttrack.DriverService.models.DriverOrderAssignment;

@Repository
public interface DriverOrderAssignmentRepository extends JpaRepository<DriverOrderAssignment, Long> { // ID is Long,
                                                                                                      // DriverId is
                                                                                                      // UUID
    Optional<DriverOrderAssignment> findByOrderIdAndDriverId(Long orderId, UUID driverId);
}
