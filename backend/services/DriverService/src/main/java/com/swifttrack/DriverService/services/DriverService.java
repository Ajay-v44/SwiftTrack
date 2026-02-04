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
import com.swifttrack.FeignClient.AuthInterface;
import com.swifttrack.dto.Message;
import com.swifttrack.dto.TokenResponse;
import com.swifttrack.dto.driverDto.AddTenantDriver;
import com.swifttrack.dto.driverDto.AddTennatDriverResponse;
import com.swifttrack.dto.driverDto.GetDriverUserDetails;
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

    @Autowired
    AuthInterface authInterface;

    @Transactional
    public Message createDriverProfile(String token, AddTenantDriver entity) {
        AddTennatDriverResponse response = authInterface.addTenantDrivers(token, entity).getBody();
        DriverVehicleDetails driverVehicleDetails = new DriverVehicleDetails();
        driverVehicleDetails.setDriverId(response.userId());
        driverVehicleDetails.setTenantId(response.tenantId());
        driverVehicleDetails.setVehicleType(entity.vehicleType());
        driverVehicleDetails.setLicenseNumber(entity.vehicleNumber());
        driverVehicleDetails.setDriverLicensNumber(entity.driverLicensNumber());
        driverVehicleDetailsRepository.save(driverVehicleDetails);
        return new Message("Driver profile created successfully");
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
        // Log Login Event
        driverEventUtil.logEvent(driverId, status.getTenantId(),
                com.swifttrack.enums.DriverEventType.LOCATION_UPDATE, "Driver location updated via UpdateStatus");
    }

    @Autowired
    private com.swifttrack.DriverService.utils.DriverEventUtil driverEventUtil;

    @Transactional
    public void toggleOnlineStatus(UUID driverId, boolean isOnline) {
        DriverStatus driverStatus = driverStatusRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver Status not found"));

        if (isOnline) {
            driverStatus.setStatus(DriverOnlineStatus.ONLINE);
            driverEventUtil.logEvent(driverId, driverStatus.getTenantId(), com.swifttrack.enums.DriverEventType.ONLINE,
                    "Driver went online");
        } else {
            driverStatus.setStatus(DriverOnlineStatus.OFFLINE);
            driverEventUtil.logEvent(driverId, driverStatus.getTenantId(), com.swifttrack.enums.DriverEventType.OFFLINE,
                    "Driver went offline");
        }
        driverStatus.setLastSeenAt(LocalDateTime.now());
        driverStatusRepository.save(driverStatus);
    }

    @Transactional
    public Message updateDriverStatus(String token, com.swifttrack.dto.driverDto.UpdateDriverStatusRequest request) {
        com.swifttrack.dto.TokenResponse userDetails = authInterface.getUserDetails(token).getBody();
        if (userDetails == null || userDetails.id() == null) {
            throw new RuntimeException("Invalid token or user not found");
        }
        UUID driverId = userDetails.id();
        UUID tenantId = userDetails.tenantId().orElse(null);
        log.info("Driver ID: {}", driverId);
        log.info("Tenant ID: {}", tenantId);
        if (!driverVehicleDetailsRepository.findByDriverId(driverId).isPresent()) {
            throw new RuntimeException("Driver profile not found. Please complete driver registration first.");
        }

        DriverStatus driverStatus = driverStatusRepository.findById(driverId)
                .orElse(new DriverStatus(driverId, tenantId, DriverOnlineStatus.OFFLINE, LocalDateTime.now()));

        // Ensure tenant matched if existing
        if (driverStatus.getTenantId() == null && tenantId != null) {
            driverStatus.setTenantId(tenantId);
        }

        driverStatus.setStatus(request.status());
        driverStatus.setLastSeenAt(LocalDateTime.now());
        driverStatusRepository.save(driverStatus);

        // Log event
        com.swifttrack.enums.DriverEventType eventType = null;
        if (request.status() == DriverOnlineStatus.ONLINE) {
            eventType = com.swifttrack.enums.DriverEventType.ONLINE;
        } else if (request.status() == DriverOnlineStatus.OFFLINE) {
            eventType = com.swifttrack.enums.DriverEventType.OFFLINE;
        }

        if (eventType != null) {
            driverEventUtil.logEvent(driverId, tenantId, eventType, "Status updated to " + request.status());
        }
        // Log Login Event
        driverEventUtil.logEvent(driverId, userDetails.tenantId().orElse(null),
                com.swifttrack.enums.DriverEventType.UPDATE_STATUS, "Driver status updated via UpdateStatus");
        return new Message("Driver status updated successfully");
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

    public TokenResponse validateToken(String token) {
        TokenResponse userDetails = authInterface.getUserDetails(token).getBody();
        if (userDetails == null || userDetails.id() == null) {
            throw new RuntimeException("Invalid token or user not found");
        }
        return userDetails;
    }

    public GetDriverUserDetails getDriverUserDetails(String token) {
        TokenResponse userDetails = authInterface.getUserDetails(token).getBody();
        if (userDetails == null || userDetails.id() == null) {
            throw new RuntimeException("Invalid token or user not found");
        }
        UUID driverId = userDetails.id();
        DriverVehicleDetails driverVehicleDetails = driverVehicleDetailsRepository.findByDriverId(driverId)
                .orElseThrow(() -> new RuntimeException("Driver profile not found"));
        DriverStatus driverStatus = driverStatusRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver Status not found"));
        // Log Login Event
        driverEventUtil.logEvent(driverId, userDetails.tenantId().orElse(null),
                com.swifttrack.enums.DriverEventType.LOGIN, "Driver Logged In via GetDetails");
        return new GetDriverUserDetails(userDetails, driverVehicleDetails.getVehicleType(),
                driverVehicleDetails.getLicenseNumber(), driverVehicleDetails.getDriverLicensNumber(),
                driverStatus.getStatus());
    }

    // --- APIs for Other Services ---

    public DriverStatus getDriverStatus(UUID driverId) {
        return driverStatusRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver Status not found"));
    }

    public DriverLocationLive getDriverLocation(UUID driverId) {
        return driverLocationLiveRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver location not found"));
    }

    public boolean isDriverAvailable(UUID driverId) {
        return driverStatusRepository.findById(driverId)
                .map(status -> status.getStatus() == DriverOnlineStatus.ONLINE)
                .orElse(false);
    }
}
