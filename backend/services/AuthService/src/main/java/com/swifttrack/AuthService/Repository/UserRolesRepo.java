package com.swifttrack.AuthService.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.swifttrack.AuthService.Models.UserRoles;
public interface UserRolesRepo extends JpaRepository<UserRoles, UUID> {
    UserRoles findByUserModelIdAndRolesId(UUID userId, UUID roleId);
    List<UserRoles> findByUserModelId(UUID userId);
}
