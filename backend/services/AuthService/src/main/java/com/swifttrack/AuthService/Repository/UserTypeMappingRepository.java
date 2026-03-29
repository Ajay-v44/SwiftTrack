package com.swifttrack.AuthService.Repository;

import com.swifttrack.AuthService.Models.UserTypeMappingModel;
import com.swifttrack.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserTypeMappingRepository extends JpaRepository<UserTypeMappingModel, UUID> {
    Optional<UserTypeMappingModel> findByUserType(UserType userType);

    List<UserTypeMappingModel> findByGroupIdAndActiveTrueOrderBySortOrderAscDisplayNameAsc(UUID groupId);
}
