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

import com.swifttrack.DriverService.enums.DriverType;
import com.swifttrack.DriverService.models.DriverLocationLive;
import com.swifttrack.DriverService.repositories.DriverLocationLiveRepository;
import com.swifttrack.DriverService.repositories.RedisGeoRepository;
import com.swifttrack.DriverService.spatial.DriverDistance;
import com.swifttrack.DriverService.spatial.DriverLocation;
import com.swifttrack.DriverService.spatial.DriverNode;
import com.swifttrack.DriverService.spatial.KDTree;

import lombok.extern.slf4j.Slf4j;

/**
 * Maintains per-category (platform / tenant:{tenantId}) driver registries and
 * KD-trees so that tenant isolation is enforced at the spatial-index level.
 */
@Service
@Slf4j
public class DriverSpatialIndexService {

    /**
     * Outer key = categoryKey ("platform" or "tenant:{id}");
     * Inner map = driverId → DriverLocation.
     */
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, DriverLocation>> driverRegistries = new ConcurrentHashMap<>();

    /**
     * One KD-tree reference per category key.
     */
    private final ConcurrentHashMap<String, AtomicReference<KDTree>> kdTreeRefs = new ConcurrentHashMap<>();

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
                    // Determine driver type based on tenantId presence
                    DriverType driverType = loc.getTenantId() != null
                            ? DriverType.TENANT_DRIVER
                            : DriverType.PLATFORM_DRIVER;
                    String tenantId = loc.getTenantId() != null ? loc.getTenantId().toString() : null;

                    DriverLocation location = new DriverLocation(
                            loc.getDriverId().toString(),
                            loc.getLatitude().doubleValue(),
                            loc.getLongitude().doubleValue(),
                            tenantId,
                            driverType);

                    String categoryKey = location.categoryKey();
                    driverRegistries
                            .computeIfAbsent(categoryKey, k -> new ConcurrentHashMap<>())
                            .put(loc.getDriverId().toString(), location);
                }
            }
            rebuildRequired.set(true);
            rebuildAllTrees();
            log.info("Initialized KD-trees: {} categories, total drivers loaded from database",
                    driverRegistries.size());
        } catch (Exception e) {
            log.warn("Failed to initialize driver KD-trees from db: {}", e.getMessage());
        }

        this.rebuildScheduler.scheduleWithFixedDelay(
                this::safeRebuildAllTrees,
                this.effectiveRebuildIntervalMs,
                this.effectiveRebuildIntervalMs,
                TimeUnit.MILLISECONDS);
        log.info("Started KD-tree rebuild scheduler with interval {} ms", this.effectiveRebuildIntervalMs);
    }

    // ─── Location update ──────────────────────────────────────────────

    public void updateDriverLocation(String driverId, double latitude, double longitude,
            String tenantId, DriverType driverType) {
        DriverLocation location = new DriverLocation(driverId, latitude, longitude, tenantId, driverType);
        String categoryKey = location.categoryKey();

        driverRegistries
                .computeIfAbsent(categoryKey, k -> new ConcurrentHashMap<>())
                .put(driverId, location);
        rebuildRequired.set(true);
    }

    // ─── Nearest-driver search ────────────────────────────────────────

    /**
     * Find nearest drivers scoped to a specific category.
     *
     * @param pickupLat  pickup latitude
     * @param pickupLon  pickup longitude
     * @param k          max drivers to return
     * @param tenantId   tenant ID (nullable for platform)
     * @param driverType TENANT_DRIVER or PLATFORM_DRIVER
     */
    public List<String> findNearestDrivers(double pickupLat, double pickupLon, int k,
            String tenantId, DriverType driverType) {
        String categoryKey = buildCategoryKey(tenantId, driverType);
        List<DriverDistance> distances = findNearestDriverDistances(pickupLat, pickupLon, k, categoryKey);
        if (distances.isEmpty()) {
            log.info("No drivers found for category '{}'", categoryKey);
            return Collections.emptyList();
        }
        return distances.stream().map(DriverDistance::driverId).toList();
    }

    public List<DriverDistance> findNearestDriverDistances(double pickupLat, double pickupLon, int k,
            String categoryKey) {
        if (k <= 0) {
            return Collections.emptyList();
        }

        // Query the category-specific Redis GEO set
        List<DriverDistance> candidates = redisGeoRepository.findNearbyDriverDistances(
                pickupLon, pickupLat, searchRadiusKm, maxRedisCandidates, categoryKey);
        log.debug("Redis candidates for '{}': {}", categoryKey, candidates.size());

        if (candidates.isEmpty()) {
            // Fallback to category-specific KD-tree
            KDTree tree = getTreeForCategory(categoryKey);
            return tree.findKNearest(pickupLat, pickupLon, k);
        }

        if (candidates.size() <= k) {
            return candidates;
        }

        return candidates.subList(0, k);
    }

    // ─── Tree rebuild ─────────────────────────────────────────────────

    public void rebuildAllTrees() {
        if (!rebuildRequired.getAndSet(false)) {
            return;
        }

        for (Map.Entry<String, ConcurrentHashMap<String, DriverLocation>> entry : driverRegistries.entrySet()) {
            String categoryKey = entry.getKey();
            ConcurrentHashMap<String, DriverLocation> registry = entry.getValue();

            List<DriverNode> snapshot = new ArrayList<>(registry.size());
            for (DriverLocation location : registry.values()) {
                snapshot.add(new DriverNode(
                        location.driverId(),
                        location.latitude(),
                        location.longitude(),
                        location.tenantId(),
                        location.driverType()));
            }

            KDTree rebuilt = KDTree.build(snapshot);
            kdTreeRefs
                    .computeIfAbsent(categoryKey, k -> new AtomicReference<>(KDTree.empty()))
                    .set(rebuilt);

            log.debug("Rebuilt KD-tree for '{}' with {} drivers", categoryKey, rebuilt.size());
        }
    }

    private void safeRebuildAllTrees() {
        try {
            rebuildAllTrees();
        } catch (Exception exception) {
            log.error("KD-tree rebuild failed", exception);
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────

    private KDTree getTreeForCategory(String categoryKey) {
        AtomicReference<KDTree> ref = kdTreeRefs.get(categoryKey);
        return ref != null ? ref.get() : KDTree.empty();
    }

    private static String buildCategoryKey(String tenantId, DriverType driverType) {
        return driverType == DriverType.PLATFORM_DRIVER
                ? "platform"
                : "tenant:" + tenantId;
    }

    @PreDestroy
    public void destroy() {
        rebuildScheduler.shutdownNow();
    }
}
