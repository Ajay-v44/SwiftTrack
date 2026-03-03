package com.swifttrack.DriverService.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.swifttrack.DriverService.models.DriverMemory;

@Repository
public interface DriverMemoryRepository extends JpaRepository<DriverMemory, UUID> {

    List<DriverMemory> findByDriverId(UUID driverId);

    /**
     * Insert a driver memory with a proper pgvector cast.
     * JPA cannot natively handle vector types, so we use a native INSERT query.
     */
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO driver_memory (id, driver_id, summary, embedding, created_at) " +
            "VALUES (:id, :driverId, :summary, CAST(:embedding AS vector), :createdAt)", nativeQuery = true)
    void insertDriverMemory(
            @Param("id") UUID id,
            @Param("driverId") UUID driverId,
            @Param("summary") String summary,
            @Param("embedding") String embedding,
            @Param("createdAt") java.time.LocalDateTime createdAt);
}
