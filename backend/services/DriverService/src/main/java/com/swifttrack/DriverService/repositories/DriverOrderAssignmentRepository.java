package com.swifttrack.DriverService.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swifttrack.DriverService.models.DriverOrderAssignment;
import com.swifttrack.enums.DriverAssignmentStatus;

@Repository
public interface DriverOrderAssignmentRepository extends JpaRepository<DriverOrderAssignment, Long> { // ID is Long,
                                                                                                      // DriverId is
                                                                                                      // UUID
    Optional<DriverOrderAssignment> findByOrderIdAndDriverId(UUID orderId, UUID driverId);

    Page<DriverOrderAssignment> findByDriverIdAndStatus(UUID driverId, DriverAssignmentStatus status,
            Pageable pageable);
}
