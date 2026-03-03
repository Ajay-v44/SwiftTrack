package com.swifttrack.DriverService.events;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.swifttrack.DriverService.dto.DriverPerformanceEvent;
import com.swifttrack.DriverService.services.DriverMemoryEmbeddingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka consumer for DriverPerformanceEvent.
 * Listens to the "driver-performance" topic and triggers
 * asynchronous memory embedding generation.
 * 
 * This ensures embedding creation NEVER blocks order flow.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DriverPerformanceEventConsumer {

    private final DriverMemoryEmbeddingService embeddingService;

    @KafkaListener(topics = "driver-performance", groupId = "driver-memory-group", containerFactory = "kafkaListenerContainerFactory", properties = {
            "spring.json.value.default.type=com.swifttrack.DriverService.dto.DriverPerformanceEvent"
    })
    public void consumePerformanceEvent(DriverPerformanceEvent event) {
        log.info("Received driver performance event: driverId={}, trigger={}",
                event.getDriverId(), event.getTriggerType());
        try {
            embeddingService.processPerformanceEvent(event);
        } catch (Exception e) {
            log.error("Error processing driver performance event for driver: {}",
                    event.getDriverId(), e);
        }
    }
}
