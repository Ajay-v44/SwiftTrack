package com.swifttrack.DriverService.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.swifttrack.DriverService.models.DriverOrderAssignment;
import com.swifttrack.enums.DriverAssignmentStatus;

@Repository
public interface DriverOrderAssignmentRepository extends JpaRepository<DriverOrderAssignment, Long> {

    Optional<DriverOrderAssignment> findByOrderIdAndDriverId(UUID orderId, UUID driverId);

    Page<DriverOrderAssignment> findByDriverIdAndStatus(UUID driverId, DriverAssignmentStatus status,
            Pageable pageable);

    Optional<DriverOrderAssignment> findByOrderId(UUID orderId);

    @Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("DELETE FROM DriverOrderAssignment a WHERE a.orderId = :orderId")
    int deleteByOrderIdInternal(@Param("orderId") UUID orderId);
}
