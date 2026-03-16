package com.swifttrack.AdminService.repositories;

import com.swifttrack.AdminService.models.AdminAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, UUID> {

    Page<AdminAuditLog> findByAdminIdOrderByCreatedAtDesc(UUID adminId, Pageable pageable);

    Page<AdminAuditLog> findByActionTypeOrderByCreatedAtDesc(String actionType, Pageable pageable);

    Page<AdminAuditLog> findByTargetIdOrderByCreatedAtDesc(UUID targetId, Pageable pageable);

    Page<AdminAuditLog> findByServiceDomainOrderByCreatedAtDesc(String serviceDomain, Pageable pageable);

    @Query("SELECT a FROM AdminAuditLog a WHERE a.createdAt BETWEEN :from AND :to ORDER BY a.createdAt DESC")
    List<AdminAuditLog> findByDateRange(LocalDateTime from, LocalDateTime to);

    @Query("SELECT a FROM AdminAuditLog a WHERE a.adminId = :adminId AND a.createdAt BETWEEN :from AND :to ORDER BY a.createdAt DESC")
    List<AdminAuditLog> findByAdminIdAndDateRange(UUID adminId, LocalDateTime from, LocalDateTime to);
}
