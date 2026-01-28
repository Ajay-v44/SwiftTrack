package com.swifttrack.DriverService.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swifttrack.DriverService.models.DriverStatus;
import java.util.UUID;

@Repository
public interface DriverStatusRepository extends JpaRepository<DriverStatus, UUID> {

}
