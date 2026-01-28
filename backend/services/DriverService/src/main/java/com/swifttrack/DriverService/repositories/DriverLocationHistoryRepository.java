package com.swifttrack.DriverService.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swifttrack.DriverService.models.DriverLocationHistory;

@Repository
public interface DriverLocationHistoryRepository extends JpaRepository<DriverLocationHistory, Long> {
    List<DriverLocationHistory> findByDriverIdAndRecordedAtBetween(UUID driverId, LocalDateTime start,
            LocalDateTime end);
}
