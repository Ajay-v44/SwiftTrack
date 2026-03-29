package com.swifttrack.AuthService.Repository;

import com.swifttrack.AuthService.Models.UserTypeGroupModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserTypeGroupRepository extends JpaRepository<UserTypeGroupModel, UUID> {
    Optional<UserTypeGroupModel> findByCodeIgnoreCase(String code);

    List<UserTypeGroupModel> findByActiveTrueOrderBySortOrderAscDisplayNameAsc();
}
