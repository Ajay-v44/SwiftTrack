package com.swifttrack.DriverService.utils;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KafkaProducerUtil {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaProducerUtil(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, Object message) {
        try {
            kafkaTemplate.send(topic, message);
            log.info("Message sent to topic: {}", topic);
        } catch (Exception e) {
            log.error("Failed to send message to topic: {}", topic, e);
        }
    }

    public void sendMessage(String topic, String key, Object message) {
        try {
            kafkaTemplate.send(topic, key, message);
            log.info("Message sent to topic: {} with key: {}", topic, key);
        } catch (Exception e) {
            log.error("Failed to send message to topic: {} with key: {}", topic, key, e);
        }
    }
}
