package com.swifttrack.DriverService.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swifttrack.DriverService.models.DriverEvent;

@Repository
public interface DriverEventRepository extends JpaRepository<DriverEvent, Long> {
    List<DriverEvent> findByDriverId(UUID driverId);
}
