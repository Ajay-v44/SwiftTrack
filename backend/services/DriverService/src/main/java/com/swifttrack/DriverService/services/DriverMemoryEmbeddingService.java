package com.swifttrack.DriverService.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.swifttrack.DriverService.dto.DriverPerformanceEvent;
import com.swifttrack.DriverService.repositories.DriverMemoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for generating behavioral summaries,
 * converting them to vector embeddings via Ollama (nomic-embed-text),
 * and storing them in the driver_memory pgvector table.
 *
 * This service is entirely asynchronous and does NOT block any order flow.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DriverMemoryEmbeddingService {

    private final EmbeddingModel embeddingModel;
    private final DriverMemoryRepository driverMemoryRepository;
    private final DriverSummaryService driverSummaryService;

    /**
     * Asynchronously creates a driver memory embedding.
     * Called from event listeners or scheduled jobs — never from request threads.
     */
    @Async
    public void processPerformanceEvent(DriverPerformanceEvent event) {
        try {
            log.info("Processing performance event for driver: {} trigger: {}",
                    event.getDriverId(), event.getTriggerType());

            // 1. Generate behavioral summary
            String summary = driverSummaryService.generateSummary(event.getDriverId());

            if (summary == null || summary.isBlank()) {
                log.warn("Empty summary generated for driver: {}, skipping embedding", event.getDriverId());
                return;
            }

            // 2. Create embedding
            createMemory(event.getDriverId().toString(), summary);

            log.info("Successfully created memory embedding for driver: {}", event.getDriverId());
        } catch (Exception e) {
            log.error("Failed to process performance event for driver: {}", event.getDriverId(), e);
        }
    }

    /**
     * Core method: generates embedding and stores in pgvector.
     */
    public void createMemory(String driverId, String summary) {
        try {
            // Call Ollama nomic-embed-text via Spring AI
            EmbeddingResponse response = embeddingModel.call(
                    new org.springframework.ai.embedding.EmbeddingRequest(
                            List.of(summary),
                            org.springframework.ai.ollama.api.OllamaOptions.builder()
                                    .model("nomic-embed-text")
                                    .build()));
            log.info("Embedding response: {}", response);
            float[] outputVector = response.getResult().getOutput();

            // Convert float[] to pgvector-compatible string format: [0.1,0.2,...]
            String vectorString = floatArrayToVectorString(outputVector);

            // Store in database using native query for proper vector casting
            driverMemoryRepository.insertDriverMemory(
                    UUID.randomUUID(),
                    UUID.fromString(driverId),
                    summary,
                    vectorString,
                    LocalDateTime.now());

            log.info("Stored embedding for driver: {} (vector dimension: {})", driverId, outputVector.length);
        } catch (Exception e) {
            log.error("Failed to create memory embedding for driver: {}", driverId, e);
        }
    }

    /**
     * Converts a float array to pgvector string format: [0.1,0.2,0.3,...]
     */
    private String floatArrayToVectorString(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0)
                sb.append(",");
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
