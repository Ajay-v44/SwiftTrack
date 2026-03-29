package com.swifttrack.AuthService.Services;

import com.swifttrack.AuthService.Dto.UserTypeGroupResponse;
import com.swifttrack.AuthService.Dto.UserTypeOptionResponse;
import com.swifttrack.AuthService.Models.UserTypeGroupModel;
import com.swifttrack.AuthService.Models.UserTypeMappingModel;
import com.swifttrack.AuthService.Repository.UserTypeGroupRepository;
import com.swifttrack.AuthService.Repository.UserTypeMappingRepository;
import com.swifttrack.enums.UserType;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserTypeCatalogService {

    private final UserTypeGroupRepository userTypeGroupRepository;
    private final UserTypeMappingRepository userTypeMappingRepository;

    public UserTypeCatalogService(
            UserTypeGroupRepository userTypeGroupRepository,
            UserTypeMappingRepository userTypeMappingRepository) {
        this.userTypeGroupRepository = userTypeGroupRepository;
        this.userTypeMappingRepository = userTypeMappingRepository;
    }

    @PostConstruct
    @Transactional
    public void initializeCatalog() {
        ensureSeedData();
    }

    @Transactional(readOnly = true)
    public List<UserTypeGroupResponse> getActiveUserTypeGroups() {
        return userTypeGroupRepository.findByActiveTrueOrderBySortOrderAscDisplayNameAsc().stream()
                .map(group -> new UserTypeGroupResponse(
                        group.getCode(),
                        group.getDisplayName(),
                        group.getDescription(),
                        userTypeMappingRepository.findByGroupIdAndActiveTrueOrderBySortOrderAscDisplayNameAsc(group.getId()).stream()
                                .map(mapping -> new UserTypeOptionResponse(
                                        mapping.getUserType(),
                                        mapping.getDisplayName(),
                                        mapping.getDescription()))
                                .toList()))
                .toList();
    }

    @Transactional
    public void ensureSeedData() {
        List<GroupSeed> seeds = defaultSeeds();

        for (GroupSeed seed : seeds) {
            UserTypeGroupModel group = userTypeGroupRepository.findByCodeIgnoreCase(seed.code())
                    .orElseGet(() -> userTypeGroupRepository.save(UserTypeGroupModel.builder()
                            .code(seed.code())
                            .displayName(seed.displayName())
                            .description(seed.description())
                            .active(true)
                            .sortOrder(seed.sortOrder())
                            .build()));

            if (!Boolean.TRUE.equals(group.getActive())
                    || !seed.displayName().equals(group.getDisplayName())
                    || !seed.description().equals(group.getDescription())
                    || !seed.sortOrder().equals(group.getSortOrder())) {
                group.setActive(true);
                group.setDisplayName(seed.displayName());
                group.setDescription(seed.description());
                group.setSortOrder(seed.sortOrder());
                userTypeGroupRepository.save(group);
            }

            for (UserTypeSeed mappingSeed : seed.userTypes()) {
                UserTypeMappingModel mapping = userTypeMappingRepository.findByUserType(mappingSeed.userType())
                        .orElseGet(() -> UserTypeMappingModel.builder()
                                .group(group)
                                .userType(mappingSeed.userType())
                                .build());

                mapping.setGroup(group);
                mapping.setDisplayName(mappingSeed.displayName());
                mapping.setDescription(mappingSeed.description());
                mapping.setActive(true);
                mapping.setSortOrder(mappingSeed.sortOrder());
                userTypeMappingRepository.save(mapping);
            }
        }
    }

    private List<GroupSeed> defaultSeeds() {
        return List.of(
                new GroupSeed(
                        "TENANT",
                        "Tenant Users",
                        "Tenant-scoped workspace roles used in the tenant dashboard.",
                        1,
                        List.of(
                                new UserTypeSeed(UserType.TENANT_ADMIN, "Tenant Admin", "Tenant owner and workspace admin", 1),
                                new UserTypeSeed(UserType.TENANT_MANAGER, "Tenant Manager", "Operations manager within a tenant", 2),
                                new UserTypeSeed(UserType.TENANT_USER, "Tenant User", "General tenant workspace user", 3),
                                new UserTypeSeed(UserType.TENANT_STAFF, "Tenant Staff", "Tenant staff member", 4),
                                new UserTypeSeed(UserType.TENANT_DRIVER, "Tenant Driver", "Driver employed by the tenant", 5))),
                new GroupSeed(
                        "PLATFORM",
                        "Platform Users",
                        "Users who operate or administer the SwiftTrack platform.",
                        2,
                        List.of(
                                new UserTypeSeed(UserType.SUPER_ADMIN, "Super Admin", "Highest privilege platform admin", 1),
                                new UserTypeSeed(UserType.SYSTEM_ADMIN, "System Admin", "Platform operations admin", 2),
                                new UserTypeSeed(UserType.SYSTEM_USER, "System User", "Internal system user", 3),
                                new UserTypeSeed(UserType.ADMIN_USER, "Admin User", "Administrative platform user", 4))),
                new GroupSeed(
                        "PROVIDER",
                        "Provider Users",
                        "Users linked to logistics providers.",
                        3,
                        List.of(
                                new UserTypeSeed(UserType.PROVIDER_USER, "Provider User", "Provider workspace user", 1))),
                new GroupSeed(
                        "DRIVER",
                        "Driver Users",
                        "Independent or platform-managed driver accounts.",
                        4,
                        List.of(
                                new UserTypeSeed(UserType.DRIVER_USER, "Driver User", "Independent driver user", 1))),
                new GroupSeed(
                        "CONSUMER",
                        "Consumer Users",
                        "Customer-facing consumer accounts.",
                        5,
                        List.of(
                                new UserTypeSeed(UserType.CONSUMER, "Consumer", "Consumer account for tracking and booking", 1))));
    }

    private record GroupSeed(
            String code,
            String displayName,
            String description,
            Integer sortOrder,
            List<UserTypeSeed> userTypes) {
    }

    private record UserTypeSeed(
            UserType userType,
            String displayName,
            String description,
            Integer sortOrder) {
    }
}
