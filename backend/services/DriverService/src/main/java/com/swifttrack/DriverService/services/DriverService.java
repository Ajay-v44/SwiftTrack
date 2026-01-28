package com.swifttrack.DriverService.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swifttrack.DriverService.models.DriverVehicleDetails;
import com.swifttrack.DriverService.models.DriverLocationLive;
import com.swifttrack.DriverService.models.DriverOrderAssignment;
import com.swifttrack.DriverService.models.DriverStatus;
import com.swifttrack.DriverService.repositories.DriverLocationLiveRepository;
import com.swifttrack.DriverService.repositories.DriverOrderAssignmentRepository;
import com.swifttrack.DriverService.repositories.DriverVehicleDetailsRepository;
import com.swifttrack.DriverService.repositories.DriverStatusRepository;
import com.swifttrack.enums.DriverAssignmentStatus;
import com.swifttrack.enums.DriverOnlineStatus;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DriverService {

    @Autowired
    private DriverVehicleDetailsRepository driverVehicleDetailsRepository;

    @Autowired
    private DriverStatusRepository driverStatusRepository;

    @Autowired
    private DriverLocationLiveRepository driverLocationLiveRepository;

    @Autowired
    private DriverOrderAssignmentRepository driverAssignmentRepository;

    @Transactional
    public DriverVehicleDetails createDriverProfile(DriverVehicleDetails driverDetails) {
        log.info("Creating new driver profile for ID: {}", driverDetails.getDriverId());
        DriverVehicleDetails savedDetails = driverVehicleDetailsRepository.save(driverDetails);

        // Initialize Status
        DriverStatus initialStatus = new DriverStatus();
        initialStatus.setDriverId(savedDetails.getDriverId());
        initialStatus.setTenantId(savedDetails.getTenantId());
        initialStatus.setStatus(DriverOnlineStatus.OFFLINE);
        initialStatus.setLastSeenAt(LocalDateTime.now());
        driverStatusRepository.save(initialStatus);

        return savedDetails;
    }

    public DriverVehicleDetails getDriverProfile(UUID driverId) {
        return driverVehicleDetailsRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver profile not found"));
    }

    @Transactional
    public void updateDriverLocation(UUID driverId, BigDecimal lat, BigDecimal lng) {
        // Update Live Location Table (HOT)
        DriverLocationLive location = driverLocationLiveRepository.findById(driverId)
                .orElse(new DriverLocationLive());

        if (location.getDriverId() == null) {
            DriverVehicleDetails details = getDriverProfile(driverId);
            location.setDriverId(driverId);
            location.setTenantId(details.getTenantId());
        }

        location.setLatitude(lat);
        location.setLongitude(lng);
        // updatedAt is handled by PreUpdate or manually
        location.setUpdatedAt(LocalDateTime.now());

        driverLocationLiveRepository.save(location);

        // Update Driver Status Last Seen
        DriverStatus status = driverStatusRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver Status not found"));
        status.setLastSeenAt(LocalDateTime.now());
        driverStatusRepository.save(status);
    }

    @Transactional
    public void toggleOnlineStatus(UUID driverId, boolean isOnline) {
        DriverStatus driverStatus = driverStatusRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver Status not found"));

        if (isOnline) {
            driverStatus.setStatus(DriverOnlineStatus.ONLINE);
        } else {
            driverStatus.setStatus(DriverOnlineStatus.OFFLINE);
        }
        driverStatus.setLastSeenAt(LocalDateTime.now());
        driverStatusRepository.save(driverStatus);
    }

    @Transactional
    public DriverOrderAssignment assignOrder(UUID driverId, Long orderId) {
        DriverStatus driverStatus = driverStatusRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver Status not found"));

        if (driverStatus.getStatus() != DriverOnlineStatus.ONLINE) {
            throw new RuntimeException("Driver is not available (OFFLINE/BUSY/SUSPENDED)");
        }

        DriverOrderAssignment assignment = new DriverOrderAssignment();
        assignment.setDriverId(driverId);
        assignment.setOrderId(orderId);
        assignment.setTenantId(driverStatus.getTenantId());
        assignment.setStatus(DriverAssignmentStatus.ASSIGNED);

        return driverAssignmentRepository.save(assignment);
    }

    @Transactional
    public void respondToAssignment(Long assignmentId, boolean accept, String reason) {
        DriverOrderAssignment assignment = driverAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (assignment.getStatus() != DriverAssignmentStatus.ASSIGNED) {
            throw new RuntimeException("Assignment is not in pending state");
        }

        if (accept) {
            assignment.setStatus(DriverAssignmentStatus.ACCEPTED);

            // Mark driver as ON_TRIP
            DriverStatus driverStatus = driverStatusRepository.findById(assignment.getDriverId())
                    .orElseThrow(() -> new RuntimeException("Driver Status not found"));
            driverStatus.setStatus(DriverOnlineStatus.ON_TRIP);
            driverStatusRepository.save(driverStatus);

        } else {
            assignment.setStatus(DriverAssignmentStatus.REJECTED);
        }
        driverAssignmentRepository.save(assignment);
    }

    public Page<DriverVehicleDetails> getDriversByTenant(UUID tenantId, Pageable pageable) {
        return driverVehicleDetailsRepository.findByTenantId(tenantId, pageable);
    }
}
