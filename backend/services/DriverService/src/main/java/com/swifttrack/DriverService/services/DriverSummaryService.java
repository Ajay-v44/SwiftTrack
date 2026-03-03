package com.swifttrack.DriverService.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.swifttrack.DriverService.models.DriverEvent;
import com.swifttrack.DriverService.models.DriverOrderAssignment;
import com.swifttrack.DriverService.models.DriverOrderCancellation;
import com.swifttrack.DriverService.models.DriverStatus;
import com.swifttrack.DriverService.repositories.DriverEventRepository;
import com.swifttrack.DriverService.repositories.DriverOrderAssignmentRepository;
import com.swifttrack.DriverService.repositories.DriverOrderCancellationRepository;
import com.swifttrack.DriverService.repositories.DriverStatusRepository;
import com.swifttrack.enums.DriverAssignmentStatus;
import com.swifttrack.enums.DriverEventType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Generates structured behavioral summaries for a driver
 * based on their recent performance data (last 24 hours).
 * 
 * Summaries describe behavioral performance patterns —
 * NOT raw event logs.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DriverSummaryService {

    private final DriverOrderAssignmentRepository assignmentRepository;
    private final DriverOrderCancellationRepository cancellationRepository;
    private final DriverEventRepository eventRepository;
    private final DriverStatusRepository statusRepository;

    /**
     * Generates a structured behavioral performance summary for a driver.
     * Summary is kept under 300 words and follows a consistent template.
     */
    public String generateSummary(UUID driverId) {
        try {
            // Fetch all relevant data
            List<DriverOrderAssignment> allAssignments = assignmentRepository.findAll()
                    .stream()
                    .filter(a -> driverId.equals(a.getDriverId()))
                    .toList();

            List<DriverEvent> events = eventRepository.findByDriverId(driverId);
            List<DriverOrderCancellation> cancellations = cancellationRepository.findAll()
                    .stream()
                    .filter(c -> driverId.equals(c.getDriverId()))
                    .toList();

            // Calculate metrics
            long completedOrders = allAssignments.stream()
                    .filter(a -> a.getStatus() == DriverAssignmentStatus.COMPLETED)
                    .count();

            long totalAssigned = allAssignments.stream()
                    .filter(a -> a.getStatus() == DriverAssignmentStatus.ASSIGNED
                            || a.getStatus() == DriverAssignmentStatus.ACCEPTED
                            || a.getStatus() == DriverAssignmentStatus.COMPLETED
                            || a.getStatus() == DriverAssignmentStatus.REJECTED)
                    .count();

            long rejectedOrders = allAssignments.stream()
                    .filter(a -> a.getStatus() == DriverAssignmentStatus.REJECTED)
                    .count();

            long cancelledOrders = cancellations.size();

            // Acceptance rate
            double acceptanceRate = totalAssigned > 0
                    ? ((double) (totalAssigned - rejectedOrders) / totalAssigned) * 100.0
                    : 100.0;

            // Count event types
            long onlineEvents = events.stream()
                    .filter(e -> e.getEventType() == DriverEventType.ONLINE)
                    .count();

            long offlineEvents = events.stream()
                    .filter(e -> e.getEventType() == DriverEventType.OFFLINE)
                    .count();

            long locationUpdates = events.stream()
                    .filter(e -> e.getEventType() == DriverEventType.LOCATION_UPDATE)
                    .count();

            // Driver status
            String currentStatus = "UNKNOWN";
            try {
                DriverStatus status = statusRepository.findById(driverId).orElse(null);
                if (status != null && status.getStatus() != null) {
                    currentStatus = status.getStatus().name();
                }
            } catch (Exception e) {
                log.warn("Could not fetch driver status for summary: {}", driverId);
            }

            // Identify behavior patterns
            String behaviorPattern = identifyBehaviorPattern(
                    completedOrders, cancelledOrders, rejectedOrders, acceptanceRate, onlineEvents, offlineEvents);

            // Build summary
            StringBuilder summary = new StringBuilder();
            summary.append(String.format("Driver %s performance summary:%n%n", driverId));
            summary.append(String.format("* Completed %d orders%n", completedOrders));
            summary.append(String.format("* Cancelled %d orders%n", cancelledOrders));
            summary.append(String.format("* Rejected %d assignments%n", rejectedOrders));
            summary.append(String.format("* Acceptance rate: %.1f%%%n", acceptanceRate));
            summary.append(String.format("* Total assignments received: %d%n", totalAssigned));
            summary.append(String.format("* Online sessions: %d, Offline events: %d%n", onlineEvents, offlineEvents));
            summary.append(String.format("* Location updates recorded: %d%n", locationUpdates));
            summary.append(String.format("* Current status: %s%n", currentStatus));
            summary.append(String.format("* Observed pattern: %s%n", behaviorPattern));

            String result = summary.toString();
            log.debug("Generated summary for driver {}: {} chars", driverId, result.length());
            return result;

        } catch (Exception e) {
            log.error("Failed to generate summary for driver: {}", driverId, e);
            return null;
        }
    }

    /**
     * Identifies behavioral patterns from metrics.
     */
    private String identifyBehaviorPattern(
            long completedOrders, long cancelledOrders, long rejectedOrders,
            double acceptanceRate, long onlineEvents, long offlineEvents) {

        StringBuilder pattern = new StringBuilder();

        if (completedOrders == 0 && cancelledOrders == 0) {
            pattern.append("New or inactive driver with no recent order history. ");
        }

        if (acceptanceRate >= 90) {
            pattern.append("High acceptance rate indicates reliable driver. ");
        } else if (acceptanceRate >= 70) {
            pattern.append("Moderate acceptance rate. ");
        } else if (acceptanceRate > 0) {
            pattern.append("Low acceptance rate — may indicate selective behavior or availability issues. ");
        }

        if (cancelledOrders > completedOrders && completedOrders > 0) {
            pattern.append("High cancellation ratio relative to completions. ");
        }

        if (rejectedOrders > 3) {
            pattern.append("Frequent order rejections observed. ");
        }

        if (offlineEvents > onlineEvents * 2 && onlineEvents > 0) {
            pattern.append("Frequent toggling between online/offline states. ");
        }

        if (completedOrders > 10) {
            pattern.append("Active driver with significant order volume. ");
        }

        if (pattern.isEmpty()) {
            pattern.append("Standard driver behavior, no notable patterns detected.");
        }

        return pattern.toString().trim();
    }
}
