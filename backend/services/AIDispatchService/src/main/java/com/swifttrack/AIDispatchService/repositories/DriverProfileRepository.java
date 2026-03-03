package com.swifttrack.AIDispatchService.repositories;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.swifttrack.AIDispatchService.dto.DriverProfile;

import lombok.RequiredArgsConstructor;

/**
 * Repository for fetching structured driver metrics from Supabase PostgreSQL.
 * 
 * IMPORTANT: All SQL is executed in the application layer only.
 * The LLM NEVER has direct database access.
 */
@Repository
@RequiredArgsConstructor
public class DriverProfileRepository {

    private static final Logger log = LoggerFactory.getLogger(DriverProfileRepository.class);

    private final JdbcTemplate jdbcTemplate;

    /**
     * Fetch structured driver profiles for the given driver IDs.
     * Computes acceptance_rate, cancellation_rate, sla_adherence, rating, idle_time.
     */
    public List<DriverProfile> fetchDriverProfiles(List<UUID> driverIds) {
        if (driverIds == null || driverIds.isEmpty()) {
            return List.of();
        }

        String placeholders = String.join(",", driverIds.stream().map(id -> "?").toList());

        String sql = """
                WITH driver_stats AS (
                    SELECT
                        dvd.driver_id,
                        COALESCE(
                            CAST(SUM(CASE WHEN doa.status = 'ACCEPTED' THEN 1 ELSE 0 END) AS DOUBLE PRECISION) /
                            NULLIF(COUNT(doa.id), 0),
                            0.5
                        ) AS acceptance_rate,
                        COALESCE(
                            CAST((SELECT COUNT(*) FROM driver_order_cancellation doc WHERE doc.driver_id = dvd.driver_id) AS DOUBLE PRECISION) /
                            NULLIF(COUNT(doa.id), 0),
                            0.0
                        ) AS cancellation_rate,
                        COALESCE(
                            CAST(SUM(CASE WHEN doa.status = 'COMPLETED' THEN 1 ELSE 0 END) AS DOUBLE PRECISION) /
                            NULLIF(SUM(CASE WHEN doa.status IN ('COMPLETED', 'ACCEPTED') THEN 1 ELSE 0 END), 0),
                            0.8
                        ) AS sla_adherence,
                        COALESCE(
                            (SELECT AVG(CAST(de.metadata::json->>'rating' AS DOUBLE PRECISION))
                             FROM driver_events de
                             WHERE de.driver_id = dvd.driver_id
                               AND de.event_type = 'RATING_UPDATED'),
                            4.0
                        ) AS rating,
                        COALESCE(
                            EXTRACT(EPOCH FROM (NOW() - ds.last_seen_at)) / 60,
                            0
                        ) AS idle_time_minutes,
                        0.0 AS distance
                    FROM driver_vehicle_details dvd
                    LEFT JOIN driver_order_assignment doa ON doa.driver_id = dvd.driver_id
                    LEFT JOIN driver_status ds ON ds.driver_id = dvd.driver_id
                    WHERE dvd.driver_id IN (%s)
                    GROUP BY dvd.driver_id, ds.last_seen_at
                )
                SELECT * FROM driver_stats
                """.formatted(placeholders);

        Object[] params = driverIds.toArray();

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> DriverProfile.builder()
                .driverId(UUID.fromString(rs.getString("driver_id")))
                .distance(BigDecimal.valueOf(rs.getDouble("distance")))
                .acceptanceRate(rs.getDouble("acceptance_rate"))
                .cancellationRate(rs.getDouble("cancellation_rate"))
                .slaAdherence(rs.getDouble("sla_adherence"))
                .rating(rs.getDouble("rating"))
                .idleTimeMinutes((long) rs.getDouble("idle_time_minutes"))
                .build());
    }
}
