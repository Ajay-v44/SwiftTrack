package com.swifttrack.DriverService.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.swifttrack.DriverService.enums.DriverType;
import com.swifttrack.DriverService.models.DriverOrderAssignment;
import com.swifttrack.DriverService.repositories.RedisGeoRepository;
import com.swifttrack.DriverService.dto.spatial.AiDispatchRequest;
import com.swifttrack.DriverService.dto.spatial.AiDispatchResponse;
import com.swifttrack.FeignClient.AIDispatchInterface;
import com.swifttrack.dto.Message;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DriverLocationService {

    private final RedisGeoRepository redisGeoRepository;
    private final DriverSpatialIndexService driverSpatialIndexService;
    private final AIDispatchInterface aiDispatchInterface;
    private final DriverService driverService;

    private final int topDispatchCount;
    private final int dispatchBatchSize;

    public DriverLocationService(
            RedisGeoRepository redisGeoRepository,
            DriverSpatialIndexService driverSpatialIndexService,
            AIDispatchInterface aiDispatchInterface,
            DriverService driverService,
            @Value("${dispatch.ai.top-drivers:15}") int topDispatchCount,
            @Value("${dispatch.ai.batch-size:5}") int dispatchBatchSize) {
        this.redisGeoRepository = redisGeoRepository;
        this.driverSpatialIndexService = driverSpatialIndexService;
        this.aiDispatchInterface = aiDispatchInterface;
        this.driverService = driverService;
        this.topDispatchCount = topDispatchCount;
        this.dispatchBatchSize = dispatchBatchSize;
    }

    /**
     * Updates a driver's location scoped to a category (platform or
     * tenant-specific).
     *
     * @param driverId   driver identifier
     * @param latitude   driver latitude
     * @param longitude  driver longitude
     * @param tenantId   tenant UUID string (nullable for platform drivers)
     * @param driverType TENANT_DRIVER or PLATFORM_DRIVER
     */
    public Message updateDriverLocation(String driverId, double latitude, double longitude,
            String tenantId, DriverType driverType) {
        validateCoordinates(latitude, longitude);

        String categoryKey = buildCategoryKey(tenantId, driverType);

        // GEO index is the coarse filter source for candidate extraction.
        redisGeoRepository.updateDriverLocation(driverId, longitude, latitude, categoryKey);

        // In-memory map update is lock-free and rebuilds are deferred to scheduler.
        driverSpatialIndexService.updateDriverLocation(driverId, latitude, longitude, tenantId, driverType);
        return new Message("Driver location updated successfully");
    }

    /**
     * Finds the nearest drivers scoped by driver type and tenant.
     *
     * @param pickupLat  pickup latitude
     * @param pickupLon  pickup longitude
     * @param k          max drivers to return
     * @param tenantId   tenant UUID string (nullable for platform)
     * @param driverType TENANT_DRIVER or PLATFORM_DRIVER
     */
    public List<String> findNearestDrivers(double pickupLat, double pickupLon, int k,
            String tenantId, DriverType driverType) {
        validateCoordinates(pickupLat, pickupLon);
        return driverSpatialIndexService.findNearestDrivers(pickupLat, pickupLon, k, tenantId, driverType);
    }

    public Optional<DriverOrderAssignment> dispatchNearestDrivers(List<String> nearestDrivers, UUID orderId,
            String token) {
        if (nearestDrivers == null || nearestDrivers.isEmpty()) {
            return Optional.empty();
        }

        List<String> ranked = nearestDrivers.size() > topDispatchCount
                ? new ArrayList<>(nearestDrivers.subList(0, topDispatchCount))
                : new ArrayList<>(nearestDrivers);

        for (int start = 0; start < ranked.size(); start += dispatchBatchSize) {
            int end = Math.min(start + dispatchBatchSize, ranked.size());
            List<String> batch = Collections.unmodifiableList(ranked.subList(start, end));

            AiDispatchResponse response = callAiDispatch(batch);
            if (response != null && response.accepted()) {
                try {
                    DriverOrderAssignment assignment = driverService.assignOrder(
                            token,
                            UUID.fromString(response.driver_id()),
                            orderId);
                    return Optional.of(assignment);
                } catch (Exception assignEx) {
                    log.warn(
                            "AI picked driver {} but assignment failed for order {}: {}",
                            response.driver_id(),
                            orderId,
                            assignEx.getMessage());
                }
            }

            log.debug("AI dispatch rejected batch {}-{}", start, end);
        }

        return Optional.empty();
    }

    private AiDispatchResponse callAiDispatch(List<String> batch) {
        try {
            ResponseEntity<AiDispatchResponse> responseEntity = aiDispatchInterface
                    .assign(new AiDispatchRequest(batch));
            return responseEntity.getBody();
        } catch (Exception ex) {
            log.warn("AI dispatch request failed for batch size {}: {}", batch.size(), ex.getMessage());
            return null;
        }
    }

    private static void validateCoordinates(double lat, double lon) {
        if (lat < -90.0d || lat > 90.0d || lon < -180.0d || lon > 180.0d) {
            throw new IllegalArgumentException("Invalid latitude/longitude");
        }
    }

    private static String buildCategoryKey(String tenantId, DriverType driverType) {
        return driverType == DriverType.PLATFORM_DRIVER
                ? "platform"
                : "tenant:" + tenantId;
    }
}
