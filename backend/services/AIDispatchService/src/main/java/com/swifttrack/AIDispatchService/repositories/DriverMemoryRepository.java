package com.swifttrack.AIDispatchService.repositories;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.swifttrack.AIDispatchService.dto.DriverMemorySummary;

import lombok.RequiredArgsConstructor;

/**
 * Repository for pgvector similarity search on driver_memory table.
 * 
 * This service does NOT generate embeddings.
 * Embeddings are pre-computed by the Driver Service.
 */
@Repository
@RequiredArgsConstructor
public class DriverMemoryRepository {

    private static final Logger log = LoggerFactory.getLogger(DriverMemoryRepository.class);

    private final JdbcTemplate jdbcTemplate;

    /**
     * Retrieve top N memory summaries per driver using pgvector cosine similarity.
     * Uses the driver's latest embedding as the query vector.
     */
    public List<DriverMemorySummary> findTopMemories(UUID driverId, int topN) {
        String sql = """
                WITH latest_embedding AS (
                    SELECT embedding
                    FROM driver_memory
                    WHERE driver_id = ?
                    ORDER BY created_at DESC
                    LIMIT 1
                )
                SELECT
                    dm.id AS memory_id,
                    dm.driver_id,
                    dm.summary,
                    1 - (dm.embedding <=> le.embedding) AS similarity_score
                FROM driver_memory dm
                CROSS JOIN latest_embedding le
                WHERE dm.driver_id = ?
                ORDER BY similarity_score DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql,
                new Object[] { driverId, driverId, topN },
                (rs, rowNum) -> DriverMemorySummary.builder()
                        .memoryId(UUID.fromString(rs.getString("memory_id")))
                        .driverId(UUID.fromString(rs.getString("driver_id")))
                        .summary(rs.getString("summary"))
                        .similarityScore(rs.getDouble("similarity_score"))
                        .build());
    }

    /**
     * Fallback: retrieve most recent memories by created_at if no embedding exists.
     */
    public List<DriverMemorySummary> findRecentMemories(UUID driverId, int topN) {
        String sql = """
                SELECT
                    id AS memory_id,
                    driver_id,
                    summary,
                    1.0 AS similarity_score
                FROM driver_memory
                WHERE driver_id = ?
                ORDER BY created_at DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql,
                new Object[] { driverId, topN },
                (rs, rowNum) -> DriverMemorySummary.builder()
                        .memoryId(UUID.fromString(rs.getString("memory_id")))
                        .driverId(UUID.fromString(rs.getString("driver_id")))
                        .summary(rs.getString("summary"))
                        .similarityScore(rs.getDouble("similarity_score"))
                        .build());
    }
}
