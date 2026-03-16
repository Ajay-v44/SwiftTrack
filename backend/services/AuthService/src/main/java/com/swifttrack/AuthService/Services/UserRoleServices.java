package com.swifttrack.AuthService.Services;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.swifttrack.AuthService.Dto.UserRoleInput;
import com.swifttrack.AuthService.Models.Roles;
import com.swifttrack.AuthService.Models.UserModel;
import com.swifttrack.AuthService.Models.UserRoles;
import com.swifttrack.AuthService.Repository.RoleRespository;
import com.swifttrack.AuthService.Repository.UserRepo;
import com.swifttrack.AuthService.Repository.UserRolesRepo;
import com.swifttrack.AuthService.util.JwtUtil;
import com.swifttrack.dto.Message;
import com.swifttrack.dto.adminDto.AssignRolesRequest;
import com.swifttrack.enums.UserType;
import com.swifttrack.exception.CustomException;
import com.swifttrack.exception.ResourceNotFoundException;

@Service
public class UserRoleServices {
    @Autowired
    UserRolesRepo userRolesRepo;

    @Autowired
    UserServices userServices;

    @Autowired
    UserRepo userRepo;

    @Autowired
    RoleRespository roleRepository;

    @Autowired
    JwtUtil jwtUtil;

    @SuppressWarnings("null")
    public Message addUserRoles(String token, UserRoleInput userRoleInput) {
        return addUserRolesInternal(token, userRoleInput.userId(), userRoleInput.roleList());
    }

    public Message addManagedUserRoles(String token, AssignRolesRequest request) {
        if (request == null || request.userId() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        return addUserRolesInternal(token, request.userId().toString(),
                request.roleIds() == null ? java.util.List.of()
                        : request.roleIds().stream().map(java.util.UUID::toString).toList());
    }

    private Message addUserRolesInternal(String token, String userId, java.util.List<String> roleIds) {
        System.out.println("Adding user roles...");
        // Get the user
        Map<String, Object> map = jwtUtil.decodeToken(token);
        System.out.println(map + "map");
        if (!map.containsKey("mobile"))
            throw new CustomException(HttpStatus.FORBIDDEN, "Forbidden");
        UserModel user = userRepo.findByMobile((String) map.get("mobile"));
        System.out.println(user + "user");
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }
        System.out.println(user.getType() + "user type");
        if (!(user.getType() == UserType.SYSTEM_ADMIN || user.getType() == UserType.SYSTEM_USER
                || user.getType() == UserType.SUPER_ADMIN || user.getType() == UserType.TENANT_ADMIN
                || user.getType() == UserType.ADMIN_USER)) {
            throw new CustomException(HttpStatus.FORBIDDEN,
                    "Forbidden - Only admins can assign roles");
        }
        UserModel userToAssignRole = userRepo.findById(UUID.fromString(userId)).orElse(null);
        if (userToAssignRole == null) {
            throw new ResourceNotFoundException("User to assign role not found");
        }
        if (user.getType() == UserType.TENANT_ADMIN
                && (user.getTenantId() == null || !user.getTenantId().equals(userToAssignRole.getTenantId()))) {
            throw new CustomException(HttpStatus.FORBIDDEN, "Tenant admin can assign roles only within own tenant");
        }
        if (roleIds == null || roleIds.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "At least one roleId is required");
        }
        // Process each role in the input
        for (String roleId : roleIds) {
            // Get the role
            Roles role = roleRepository.findById(UUID.fromString(roleId.toString())).orElse(null);
            if (role == null) {
                continue; // Skip invalid roles
            }
            if (userRolesRepo.findByUserModelIdAndRolesId(userToAssignRole.getId(), role.getId()) != null) {
                continue; // Skip if user already has the role
            }
            // Create a new UserRoles entity
            UserRoles userRole = new UserRoles();
            userRole.setUserModel(userToAssignRole);
            userRole.setRoles(role);
            userRole.setStatus(true); // Assuming active status by default

            // Save the user-role mapping
            userRolesRepo.save(userRole);
        }

        return new Message("User roles assigned successfully");

    }
}
