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

    @Autowired
    private com.swifttrack.DriverService.events.DriverLocationProducer driverLocationProducer;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Async
    public void logEvent(UUID driverId, UUID tenantId, DriverEventType eventType, String metadata) {
        try {
            DriverEvent event = new DriverEvent();
            event.setDriverId(driverId);
            event.setTenantId(tenantId);
            event.setEventType(eventType);

            String jsonMetadata = metadata;
            if (metadata == null) {
                jsonMetadata = "{}";
            } else if (!metadata.trim().startsWith("{") && !metadata.trim().startsWith("[")) {
                try {
                    java.util.Map<String, String> map = new java.util.HashMap<>();
                    map.put("message", metadata);
                    jsonMetadata = objectMapper.writeValueAsString(map);
                } catch (Exception e) {
                    log.warn("Failed to wrap metadata string into JSON", e);
                    jsonMetadata = "{}";
                }
            }

            event.setMetadata(jsonMetadata);
            event.setCreatedAt(LocalDateTime.now());
            driverEventRepository.save(event);

            // Publish event to Kafka
            driverLocationProducer.publishDriverEvent(event);

            log.info("Logged and published driver event: {} for driver: {}", eventType, driverId);
        } catch (Exception e) {
            log.error("Failed to log/publish driver event: {} for driver: {}", eventType, driverId, e);
        }
    }
}
