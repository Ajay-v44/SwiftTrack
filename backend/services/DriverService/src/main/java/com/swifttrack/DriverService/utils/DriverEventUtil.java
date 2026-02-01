package com.swifttrack.DriverService.utils;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.swifttrack.DriverService.models.DriverEvent;
import com.swifttrack.DriverService.repositories.DriverEventRepository;
import com.swifttrack.enums.DriverEventType;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DriverEventUtil {

    @Autowired
    private DriverEventRepository driverEventRepository;

    @Async
    public void logEvent(UUID driverId, UUID tenantId, DriverEventType eventType, String metadata) {
        try {
            DriverEvent event = new DriverEvent();
            event.setDriverId(driverId);
            event.setTenantId(tenantId);
            event.setEventType(eventType);
            event.setMetadata(metadata);
            event.setCreatedAt(LocalDateTime.now());
            driverEventRepository.save(event);
            log.info("Logged driver event: {} for driver: {}", eventType, driverId);
        } catch (Exception e) {
            log.error("Failed to log driver event: {} for driver: {}", eventType, driverId, e);
        }
    }
}
