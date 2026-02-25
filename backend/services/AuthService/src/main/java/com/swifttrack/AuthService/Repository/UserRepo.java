package com.swifttrack.AuthService.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.swifttrack.AuthService.Models.UserModel;
import com.swifttrack.enums.UserType;

public interface UserRepo extends JpaRepository<UserModel, UUID> {
    UserModel findByName(String username);

    UserModel findByEmail(String email);

    UserModel findByMobile(String mobile);

    @Query("SELECT u FROM UserModel u WHERE u.tenantId = :tenantId AND u.type = :userType")
    List<UserModel> findByTenantId(@Param("tenantId") UUID tenantId, @Param("userType") UserType userType);

    @Query("SELECT u FROM UserModel u WHERE u.type in (:userType)")
    List<UserModel> findByType(@Param("userType") List<UserType> userType);
}