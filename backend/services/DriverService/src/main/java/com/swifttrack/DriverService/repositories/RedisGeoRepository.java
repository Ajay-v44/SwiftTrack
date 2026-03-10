package com.swifttrack.DriverService.repositories;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import com.swifttrack.DriverService.spatial.DriverDistance;

@Repository
public class RedisGeoRepository {

    private final StringRedisTemplate stringRedisTemplate;
    private final String geoKeyPrefix;

    public RedisGeoRepository(
            StringRedisTemplate stringRedisTemplate,
            @Value("${dispatch.redis.geo-key-prefix:drivers}") String geoKeyPrefix) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.geoKeyPrefix = geoKeyPrefix;
    }

    /**
     * Builds the Redis GEO key for a given category.
     * Example: "drivers:platform" or "drivers:tenant:abc-123".
     */
    private String geoKeyFor(String categoryKey) {
        return geoKeyPrefix + ":" + categoryKey;
    }

    /**
     * Updates the driver location in the Redis GEO set scoped by category.
     *
     * @param driverId    driver identifier
     * @param longitude   driver longitude
     * @param latitude    driver latitude
     * @param categoryKey "platform" or "tenant:{tenantId}"
     */
    public void updateDriverLocation(String driverId, double longitude, double latitude, String categoryKey) {
        GeoOperations<String, String> geo = stringRedisTemplate.opsForGeo();
        geo.add(geoKeyFor(categoryKey), new Point(longitude, latitude), driverId);
    }

    public List<String> findNearbyDriverIds(double longitude, double latitude, double radiusKm,
            int maxResults, String categoryKey) {
        return findNearbyDriverDistances(longitude, latitude, radiusKm, maxResults, categoryKey)
                .stream().map(DriverDistance::driverId).toList();
    }

    public List<DriverDistance> findNearbyDriverDistances(double longitude, double latitude, double radiusKm,
            int maxResults, String categoryKey) {
        GeoOperations<String, String> geo = stringRedisTemplate.opsForGeo();

        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .sortAscending()
                .includeDistance()
                .limit(maxResults);

        GeoResults<RedisGeoCommands.GeoLocation<String>> results = geo.radius(
                geoKeyFor(categoryKey),
                new Circle(new Point(longitude, latitude), new Distance(radiusKm, Metrics.KILOMETERS)),
                args);

        if (results == null || results.getContent().isEmpty()) {
            return Collections.emptyList();
        }

        return results.getContent().stream()
                .filter(result -> result.getContent().getName() != null)
                .map(result -> new DriverDistance(result.getContent().getName(), result.getDistance().getValue()))
                .toList();
    }
}
