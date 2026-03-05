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
    private final String geoKey;

    public RedisGeoRepository(
            StringRedisTemplate stringRedisTemplate,
            @Value("${dispatch.redis.geo-key:drivers}") String geoKey) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.geoKey = geoKey;
    }

    public void updateDriverLocation(String driverId, double longitude, double latitude) {
        GeoOperations<String, String> geo = stringRedisTemplate.opsForGeo();
        geo.add(geoKey, new Point(longitude, latitude), driverId);
    }

    public List<String> findNearbyDriverIds(double longitude, double latitude, double radiusKm, int maxResults) {
        return findNearbyDriverDistances(longitude, latitude, radiusKm, maxResults)
                .stream().map(DriverDistance::driverId).toList();
    }

    public List<DriverDistance> findNearbyDriverDistances(double longitude, double latitude, double radiusKm,
            int maxResults) {
        GeoOperations<String, String> geo = stringRedisTemplate.opsForGeo();

        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .sortAscending()
                .includeDistance()
                .limit(maxResults);

        GeoResults<RedisGeoCommands.GeoLocation<String>> results = geo.radius(
                geoKey,
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
