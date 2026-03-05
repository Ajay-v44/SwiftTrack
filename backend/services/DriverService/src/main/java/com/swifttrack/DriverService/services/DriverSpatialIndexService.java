package com.swifttrack.DriverService.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.swifttrack.DriverService.models.DriverLocationLive;
import com.swifttrack.DriverService.repositories.DriverLocationLiveRepository;
import com.swifttrack.DriverService.repositories.RedisGeoRepository;
import com.swifttrack.DriverService.spatial.DriverDistance;
import com.swifttrack.DriverService.spatial.DriverLocation;
import com.swifttrack.DriverService.spatial.DriverNode;
import com.swifttrack.DriverService.spatial.KDTree;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DriverSpatialIndexService {

    private final ConcurrentHashMap<String, DriverLocation> driverRegistry = new ConcurrentHashMap<>();
    private final AtomicReference<KDTree> kdTreeRef = new AtomicReference<>(KDTree.empty());
    private final AtomicBoolean rebuildRequired = new AtomicBoolean(false);
    private final ScheduledExecutorService rebuildScheduler;

    private final RedisGeoRepository redisGeoRepository;
    private final DriverLocationLiveRepository driverLocationLiveRepository;
    private final double searchRadiusKm;
    private final int maxRedisCandidates;
    private final long effectiveRebuildIntervalMs;

    public DriverSpatialIndexService(
            RedisGeoRepository redisGeoRepository,
            DriverLocationLiveRepository driverLocationLiveRepository,
            @Value("${dispatch.search.radius-km:5}") double searchRadiusKm,
            @Value("${dispatch.search.max-redis-candidates:200}") int maxRedisCandidates,
            @Value("${dispatch.kdtree.rebuild-interval-ms:2000}") long rebuildIntervalMs) {
        this.redisGeoRepository = redisGeoRepository;
        this.driverLocationLiveRepository = driverLocationLiveRepository;
        this.searchRadiusKm = searchRadiusKm;
        this.maxRedisCandidates = maxRedisCandidates;
        this.effectiveRebuildIntervalMs = rebuildIntervalMs <= 0 ? 2000L : Math.max(200L, rebuildIntervalMs);
        this.rebuildScheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "driver-kdtree-rebuild");
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    @PostConstruct
    public void startRebuildScheduler() {
        try {
            List<DriverLocationLive> liveLocations = driverLocationLiveRepository.findAll();
            for (DriverLocationLive loc : liveLocations) {
                if (loc.getLatitude() != null && loc.getLongitude() != null) {
                    DriverLocation location = new DriverLocation(
                            loc.getDriverId().toString(),
                            loc.getLatitude().doubleValue(),
                            loc.getLongitude().doubleValue());
                    driverRegistry.put(loc.getDriverId().toString(), location);
                }
            }
            rebuildTree();
            log.info("Initialized KD-tree with {} drivers from database", driverRegistry.size());
        } catch (Exception e) {
            log.warn("Failed to initialize driver KD-tree from db: {}", e.getMessage());
        }

        this.rebuildScheduler.scheduleWithFixedDelay(
                this::safeRebuildTree,
                this.effectiveRebuildIntervalMs,
                this.effectiveRebuildIntervalMs,
                TimeUnit.MILLISECONDS);
        log.info("Started KD-tree rebuild scheduler with interval {} ms", this.effectiveRebuildIntervalMs);
    }

    public void updateDriverLocation(String driverId, double latitude, double longitude) {
        driverRegistry.put(driverId, new DriverLocation(driverId, latitude, longitude));
        rebuildRequired.set(true);
    }

    public void rebuildTree() {
        if (!rebuildRequired.getAndSet(false)) {
            return;
        }

        List<DriverNode> snapshot = new ArrayList<>(driverRegistry.size());
        for (Map.Entry<String, DriverLocation> entry : driverRegistry.entrySet()) {
            DriverLocation location = entry.getValue();
            snapshot.add(new DriverNode(location.driverId(), location.latitude(), location.longitude()));
        }

        KDTree rebuilt = KDTree.build(snapshot);
        kdTreeRef.set(rebuilt);

        log.debug("Rebuilt driver KD-tree with {} drivers", rebuilt.size());
    }

    private void safeRebuildTree() {
        try {
            rebuildTree();
        } catch (Exception exception) {
            log.error("KD-tree rebuild failed", exception);
        }
    }

    public List<String> findNearestDrivers(double pickupLat, double pickupLon, int k) {
        List<DriverDistance> distances = findNearestDriverDistances(pickupLat, pickupLon, k);
        if (distances.isEmpty()) {
            System.out.println("No drivers found");
            return Collections.emptyList();
        }

        return distances.stream().map(DriverDistance::driverId).toList();
    }

    public List<DriverDistance> findNearestDriverDistances(double pickupLat, double pickupLon, int k) {
        if (k <= 0) {
            return Collections.emptyList();
        }

        List<DriverDistance> candidates = redisGeoRepository.findNearbyDriverDistances(
                pickupLon,
                pickupLat,
                searchRadiusKm,
                maxRedisCandidates);
        System.out.println("Candidates: " + candidates);

        if (candidates.isEmpty()) {
            return kdTreeRef.get().findKNearest(pickupLat, pickupLon, k);
        }

        if (candidates.size() <= k) {
            return candidates;
        }

        return candidates.subList(0, k);
    }

    @PreDestroy
    public void destroy() {
        rebuildScheduler.shutdownNow();
    }
}
