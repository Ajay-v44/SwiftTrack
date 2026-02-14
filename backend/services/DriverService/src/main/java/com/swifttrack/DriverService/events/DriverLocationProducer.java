package com.swifttrack.DriverService.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.swifttrack.DriverService.models.DriverEvent;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DriverLocationProducer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "driver.location.updates";

    public void publishLocationUpdate(String driverId, double lat, double lng) {
        // Payload would typically be a specific Avro/JSON object
        // String payload = ...;
        // kafkaTemplate.send(TOPIC, driverId, payload);
        log.info("Publishing location update for driver {}: {}, {}", driverId, lat, lng);
    }

    private static final String EVENT_TOPIC = "driver.events";

    public void publishDriverEvent(DriverEvent event) {
        kafkaTemplate.send(EVENT_TOPIC, event.getDriverId().toString(), event);
        log.info("Published driver event: {}", event);
    }
}
