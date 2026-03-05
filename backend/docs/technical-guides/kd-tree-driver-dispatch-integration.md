# KD-Tree Driver Dispatch Integration (DriverService)

## Overview
This document describes the KD-Tree + Redis GEO based nearest-driver flow implemented in `DriverService`.

The integration has three goals:
1. Keep a high-throughput in-memory spatial index for precise nearest-neighbor lookups.
2. Use Redis GEO for fast coarse filtering before KD-tree search.
3. Dispatch nearest drivers to AI Dispatch, then assign the order to the selected driver.

## Components

### 1. DriverSpatialIndexService
File: `services/DriverService/src/main/java/com/swifttrack/DriverService/services/DriverSpatialIndexService.java`

Responsibilities:
- Maintain `ConcurrentHashMap<String, DriverLocation>` as source-of-truth in-memory registry.
- Maintain `AtomicReference<KDTree>` for lock-free read path.
- Rebuild KD-tree on a fixed interval (default 2s), only when updates occurred.

Concurrency model:
- Write path (`updateDriverLocation`) only updates map + dirty flag (`AtomicBoolean`).
- Read path (`findNearestDrivers`) is non-blocking and does not lock updates.
- Rebuild path snapshots map and atomically swaps KD-tree reference.

Scheduler:
- Uses `ScheduledExecutorService` with single daemon thread.
- Starts via `@PostConstruct` and stops via `@PreDestroy`.
- Interval is configurable by `dispatch.kdtree.rebuild-interval-ms`.
- Guard rails:
  - `<= 0` becomes `2000ms`
  - minimum interval is capped at `200ms` to avoid accidental overload.

### 2. KDTree
File: `services/DriverService/src/main/java/com/swifttrack/DriverService/spatial/KDTree.java`

Capabilities:
- Build KD-tree from candidate nodes.
- K-nearest-neighbor search using a max-heap (`PriorityQueue`) to keep top K closest.
- Distance metric: Haversine (kilometers).

Complexity notes:
- Build: `O(n log n)` average with quickselect partitioning.
- Search: `O(log n)` average, `O(n)` worst-case.

### 3. RedisGeoRepository
File: `services/DriverService/src/main/java/com/swifttrack/DriverService/repositories/RedisGeoRepository.java`

Responsibilities:
- `GEOADD` on location update.
- Radius search for coarse candidate filtering (`5 km`, max `200` candidates by default).

### 4. DriverLocationService
File: `services/DriverService/src/main/java/com/swifttrack/DriverService/services/DriverLocationService.java`

Responsibilities:
- Accept location updates and write to Redis GEO + in-memory registry.
- Compute nearest drivers using Redis coarse filter + KD-tree precise ranking.
- Batch nearest drivers to AI Dispatch via Feign.
- When AI returns a selected `driver_id`, assign order to that driver immediately.

Assignment behavior:
- AI dispatch runs in batches of 5 from top 15 nearest drivers.
- If selected driver assignment fails (busy, already assigned, invalid state), flow continues to next batch.

### 5. Feign AI Client
File: `services/DriverService/src/main/java/com/swifttrack/FeignClient/AIDispatchInterface.java`

Endpoint:
- `POST /dispatch/assign`

Payload:
```json
{ "driverIds": ["uuid1", "uuid2", "uuid3", "uuid4", "uuid5"] }
```

## APIs

### Update location (existing API)
`POST /api/driver/v1/location`

Request:
```json
{
  "latitude": 12.915,
  "longitude": 77.605
}
```

Behavior:
- Existing DB update flow remains unchanged.
- Spatial-index integration added in the same endpoint:
  - writes to Redis GEO
  - updates in-memory registry
  - marks KD-tree for scheduled rebuild

### Find nearest and assign order
`POST /drivers/assign-nearest`
```json
{
  "pickupLat": 12.915,
  "pickupLon": 77.605,
  "orderId": "uuid"
}
```

Header required:
- `token`: auth token used by DriverService to assign order internally.

Note:
- `k` is internal/static (`15`) and is not accepted in request payload.

Response:
```json
{
  "candidateDrivers": ["id1", "id2", "id3"]
}
```

## Config
File: `services/DriverService/src/main/resources/application.yaml`

```yaml
dispatch:
  redis:
    geo-key: drivers
  search:
    radius-km: 5
    max-redis-candidates: 200
  kdtree:
    rebuild-interval-ms: 2000
  ai:
    base-url: http://ai-dispatch-service:8010
    top-drivers: 15
    batch-size: 5
```

## End-to-end sequence
1. Driver posts location.
2. Service updates Redis GEO and in-memory registry.
3. Rebuild thread rebuilds KD-tree every configured interval when dirty.
4. Order dispatch API called with pickup location and orderId.
5. Redis GEO returns nearby candidates.
6. KD-tree ranks nearest K precisely.
7. Drivers sent to AI Dispatch in batches.
8. AI-selected driver is assigned to order via DriverService assignment logic.
9. If assignment fails, next AI batch is attempted.

## Operational notes
- For very high update rates, keep rebuild interval at `2s` or tune between `500ms` and `3000ms` based on CPU/latency tradeoff.
- Keep Redis and DriverService in same low-latency network zone.
- Add metrics around:
  - redis candidate count
  - kdtree rebuild duration
  - nearest search duration
  - ai dispatch latency
  - assignment success/failure counts
