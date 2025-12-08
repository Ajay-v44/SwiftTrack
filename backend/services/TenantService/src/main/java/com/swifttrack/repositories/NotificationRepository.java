package com.swifttrack.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swifttrack.Models.NotificationPreferences;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationPreferences,UUID> {

    NotificationPreferences findByTenantId(UUID tenantId);

}
