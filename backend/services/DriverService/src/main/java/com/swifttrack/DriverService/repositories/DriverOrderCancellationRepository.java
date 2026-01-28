package com.swifttrack.DriverService.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swifttrack.DriverService.models.DriverOrderCancellation;

@Repository
public interface DriverOrderCancellationRepository extends JpaRepository<DriverOrderCancellation, Long> {
}
