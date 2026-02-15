package com.swifttrack.DriverService.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swifttrack.DriverService.models.DriverVehicleDetails;
import com.swifttrack.DriverService.dto.UpdateOrderStatusrequest;
import com.swifttrack.DriverService.models.DriverLocationLive;
import com.swifttrack.DriverService.models.DriverOrderAssignment;
import com.swifttrack.DriverService.models.DriverStatus;
import com.swifttrack.DriverService.repositories.DriverLocationLiveRepository;
import com.swifttrack.DriverService.repositories.DriverOrderAssignmentRepository;
import com.swifttrack.DriverService.repositories.DriverVehicleDetailsRepository;
import com.swifttrack.FeignClient.AuthInterface;
import com.swifttrack.dto.ListOfTenantUsers;
import com.swifttrack.dto.Message;
import com.swifttrack.dto.TokenResponse;
import com.swifttrack.dto.driverDto.AddTenantDriver;
import com.swifttrack.dto.driverDto.AddTennatDriverResponse;
import com.swifttrack.dto.driverDto.GetDriverUserDetails;
import com.swifttrack.dto.driverDto.GetTenantDrivers;
import com.swifttrack.dto.driverDto.UpdateDriverStatusRequest;
import com.swifttrack.DriverService.repositories.DriverStatusRepository;
import com.swifttrack.enums.DriverAssignmentStatus;
import com.swifttrack.enums.DriverOnlineStatus;
import com.swifttrack.enums.TrackingStatus;
import com.swifttrack.enums.UserType;
import com.swifttrack.events.DriverLocationUpdates;

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

    @Autowired
    com.swifttrack.FeignClient.OrderInterface orderInterface;

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
        driverEventUtil.addDriverPreviousLocation(driverId, location.getTenantId(), location.getLatitude(),
                location.getLongitude());
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

    @Autowired
    private com.swifttrack.DriverService.utils.KafkaProducerUtil kafkaProducerUtil;

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
    public DriverOrderAssignment assignOrder(String token, UUID driverId, UUID orderId) {
        // Check if order exists
        try {
            orderInterface.getOrderById(token, orderId);
        } catch (Exception e) {
            throw new RuntimeException("Order not found or invalid Order ID");
        }

        if (driverAssignmentRepository.findByOrderId(orderId).isPresent()) {
            throw new RuntimeException("Order is already assigned to a driver");
        }
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
    public void respondToAssignment(String token, UUID orderId, boolean accept, String reason) {
        DriverOrderAssignment assignment = driverAssignmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (assignment.getStatus() != DriverAssignmentStatus.ASSIGNED) {
            throw new RuntimeException("Assignment is not in pending state");
        }
        // Mark driver as ON_TRIP
        DriverStatus driverStatus = driverStatusRepository.findById(assignment.getDriverId())
                .orElseThrow(() -> new RuntimeException("Driver Status not found"));
        if (accept) {
            assignment.setStatus(DriverAssignmentStatus.ACCEPTED);

            driverStatus.setStatus(DriverOnlineStatus.ON_TRIP);
            driverStatusRepository.save(driverStatus);
            driverEventUtil.logEvent(assignment.getDriverId(), assignment.getTenantId(),
                    com.swifttrack.enums.DriverEventType.ORDER_ACCEPTED, "Order accepted by driver");
            driverAssignmentRepository.save(assignment);

            // Fetch Driver Details from Auth Service
            TokenResponse userDetails = authInterface.getUserDetails(token).getBody();
            DriverVehicleDetails vehicleDetails = driverVehicleDetailsRepository
                    .findByDriverId(assignment.getDriverId())
                    .orElse(new DriverVehicleDetails());

            // Create and Send DriverAssignedEvent
            com.swifttrack.events.DriverAssignedEvent event = com.swifttrack.events.DriverAssignedEvent.builder()
                    .orderId(orderId)
                    .driverName(userDetails.name())
                    .driverPhone(userDetails.mobile())
                    .vehicleNumber(vehicleDetails.getLicenseNumber()) // Assuming licenseNumber is vehicle number
                    .providerCode(userDetails.providerId().map(UUID::toString)
                            .orElse(userDetails.tenantId().map(UUID::toString).orElse("UNKNOWN")))
                    .build();

            kafkaProducerUtil.sendMessage("driver-assigned", event);

        } else {
            driverStatus.setStatus(DriverOnlineStatus.ONLINE);
            driverStatusRepository.save(driverStatus);
            assignment.setStatus(DriverAssignmentStatus.REJECTED);
            driverEventUtil.logEvent(assignment.getDriverId(), assignment.getTenantId(),
                    com.swifttrack.enums.DriverEventType.ORDER_CANCELLED, "Order rejected by driver");
            driverAssignmentRepository.delete(assignment);

            // Send Driver Canceled Event
            kafkaProducerUtil.sendMessage("driver-canceled", orderId);
        }
    }

    public List<com.swifttrack.dto.orderDto.GetOrdersForDriver> getMyOrders(String token, DriverAssignmentStatus status,
            int page, int size) {
        TokenResponse userDetails = authInterface.getUserDetails(token).getBody();
        UUID driverId = userDetails.id();

        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        Page<DriverOrderAssignment> assignments = driverAssignmentRepository.findByDriverIdAndStatus(driverId, status,
                pageable);

        List<UUID> orderIds = assignments.getContent().stream()
                .map(DriverOrderAssignment::getOrderId)
                .collect(java.util.stream.Collectors.toList());

        if (orderIds.isEmpty()) {
            return new ArrayList<>();
        }
        System.out.println("Order IDs: " + orderIds);
        return orderInterface.getOrdersForDriver(token, new com.swifttrack.dto.orderDto.GetOrdersRequest(orderIds))
                .getBody();
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

    public List<GetTenantDrivers> getTenantDrivers(String token) {
        List<ListOfTenantUsers> tenantUsers = authInterface.getTenantUsers(token, UserType.TENANT_DRIVER).getBody();
        List<GetTenantDrivers> driverDetailsList = new ArrayList<>();

        if (tenantUsers != null) {
            for (ListOfTenantUsers user : tenantUsers) {
                DriverVehicleDetails vehicleDetails = driverVehicleDetailsRepository
                        .findByDriverId(user.id())
                        .orElse(new DriverVehicleDetails());

                DriverStatus driverStatus = driverStatusRepository
                        .findById(user.id())
                        .orElse(new DriverStatus());

                String vType = (vehicleDetails.getVehicleType() != null) ? vehicleDetails.getVehicleType().name()
                        : null;

                GetTenantDrivers dto = new GetTenantDrivers(
                        user.id(),
                        user.name(),
                        user.email(),
                        user.mobile(),
                        vType,
                        vehicleDetails.getLicenseNumber(),
                        vehicleDetails.getDriverLicensNumber(),
                        driverStatus.getStatus(),
                        driverStatus.getLastSeenAt(),
                        vehicleDetails.getCreatedAt(),
                        vehicleDetails.getUpdatedAt());
                driverDetailsList.add(dto);
            }
        }
        return driverDetailsList;
    }

    public Message updateOrderStatus(String token, UpdateOrderStatusrequest request) {
        String orderStatus = orderInterface.getOrderStatus(token, request.orderId()).getBody();
        TokenResponse userDetails = authInterface.getUserDetails(token).getBody();
        DriverLocationLive driverLocationLive = driverLocationLiveRepository.findById(userDetails.id())
                .orElseThrow(() -> new RuntimeException("Driver location not found"));
        if (orderStatus.equals("PICKED_UP") && request.status() != TrackingStatus.IN_TRANSIT) {
            throw new RuntimeException("Invalid status transition, order is not picked up");
        } else if (orderStatus.equals("IN_TRANSIT") && request.status() != TrackingStatus.OUT_FOR_DELIVERY) {
            throw new RuntimeException("Invalid status transition, order is not in transit");
        } else if (orderStatus.equals("OUT_FOR_DELIVERY") && request.status() != TrackingStatus.DELIVERED) {
            throw new RuntimeException("Invalid status transition, order is not out for delivery");
        }
        if (orderStatus.equals("DELIVERED")) {
            DriverStatus driverStatus = driverStatusRepository.findById(userDetails.id())
                    .orElseThrow(() -> new RuntimeException("Driver Status not found"));
            driverStatus.setStatus(DriverOnlineStatus.ONLINE);
            driverStatusRepository.save(driverStatus);
        }

        DriverLocationUpdates driverLocationUpdates = new DriverLocationUpdates().builder()
                .orderId(request.orderId())
                .status(request.status().toString())
                .latitude(driverLocationLive.getLatitude())
                .longitude(driverLocationLive.getLongitude())
                .build();
        kafkaProducerUtil.sendMessage("driver-location-updates", driverLocationUpdates);
        return new Message("Order status updated successfully");
    }
}
