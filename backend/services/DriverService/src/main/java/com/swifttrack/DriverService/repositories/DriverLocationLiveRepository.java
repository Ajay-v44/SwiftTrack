package com.swifttrack.DriverService.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swifttrack.DriverService.models.DriverLocationLive;
import java.util.UUID;

@Repository
public interface DriverLocationLiveRepository extends JpaRepository<DriverLocationLive, UUID> {
}
