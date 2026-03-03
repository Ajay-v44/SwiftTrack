package com.swifttrack.DriverService.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.swifttrack.DriverService.dto.DriverPerformanceEvent;
import com.swifttrack.DriverService.models.DriverStatus;
import com.swifttrack.DriverService.repositories.DriverStatusRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduled job that runs daily to aggregate all active drivers'
 * performance data and create/update their memory embeddings.
 * 
 * This provides a baseline refresh for embeddings regardless of
 * individual event triggers.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DriverMemoryScheduledJob {

    private final DriverStatusRepository driverStatusRepository;
    private final DriverMemoryEmbeddingService embeddingService;

    /**
     * Runs every day at 2:00 AM IST.
     * Iterates all known drivers and generates fresh memory embeddings.
     */
    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Kolkata")
    public void dailyAggregation() {
        log.info("Starting daily driver memory aggregation job...");

        try {
            List<DriverStatus> allDrivers = driverStatusRepository.findAll();
            List<UUID> driverIds = allDrivers.stream()
                    .map(DriverStatus::getDriverId)
                    .collect(Collectors.toList());

            log.info("Found {} drivers for daily aggregation", driverIds.size());

            int successCount = 0;
            int errorCount = 0;

            for (UUID driverId : driverIds) {
                try {
                    DriverPerformanceEvent event = DriverPerformanceEvent.builder()
                            .driverId(driverId)
                            .triggerType(DriverPerformanceEvent.TriggerType.DAILY_AGGREGATION)
                            .build();

                    embeddingService.processPerformanceEvent(event);
                    successCount++;
                } catch (Exception e) {
                    log.error("Failed daily aggregation for driver: {}", driverId, e);
                    errorCount++;
                }
            }

            log.info("Daily aggregation completed. Success: {}, Errors: {}", successCount, errorCount);

        } catch (Exception e) {
            log.error("Daily driver memory aggregation job failed", e);
        }
    }
}
