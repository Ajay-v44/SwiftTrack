package com.swifttrack.AdminService.services;

import com.swifttrack.AdminService.models.AdminAuditLog;
import com.swifttrack.AdminService.repositories.AdminAuditLogRepository;
import com.swifttrack.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Asynchronous audit logging service.
 * All admin mutations should call log() to create a non-blocking audit trail.
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AdminAuditLogRepository auditLogRepository;

    /**
     * Fire-and-forget async audit log write.
     * Runs on a separate thread to not add latency to API responses.
     */
    @Async
    public void log(TokenResponse admin,
                    String actionType,
                    String serviceDomain,
                    UUID targetId,
                    String targetType,
                    String details) {
        try {
            AdminAuditLog log = AdminAuditLog.builder()
                    .adminId(admin.id())
                    .adminName(admin.name())
                    .adminType(admin.userType().map(Enum::name).orElse("UNKNOWN"))
                    .actionType(actionType)
                    .serviceDomain(serviceDomain)
                    .targetId(targetId)
                    .targetType(targetType)
                    .details(details)
                    .build();
            auditLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("[ADMIN-AUDIT] Failed to write audit log: " + e.getMessage());
        }
    }

    public Page<AdminAuditLog> getAuditLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return auditLogRepository.findAll(pageable);
    }

    public Page<AdminAuditLog> getAuditLogsByAdmin(UUID adminId, int page, int size) {
        return auditLogRepository.findByAdminIdOrderByCreatedAtDesc(adminId, PageRequest.of(page, size));
    }

    public Page<AdminAuditLog> getAuditLogsByActionType(String actionType, int page, int size) {
        return auditLogRepository.findByActionTypeOrderByCreatedAtDesc(actionType, PageRequest.of(page, size));
    }

    public Page<AdminAuditLog> getAuditLogsByTarget(UUID targetId, int page, int size) {
        return auditLogRepository.findByTargetIdOrderByCreatedAtDesc(targetId, PageRequest.of(page, size));
    }

    public Page<AdminAuditLog> getAuditLogsByDomain(String domain, int page, int size) {
        return auditLogRepository.findByServiceDomainOrderByCreatedAtDesc(domain, PageRequest.of(page, size));
    }

    public List<AdminAuditLog> getAuditLogsByDateRange(LocalDateTime from, LocalDateTime to) {
        return auditLogRepository.findByDateRange(from, to);
    }
}
