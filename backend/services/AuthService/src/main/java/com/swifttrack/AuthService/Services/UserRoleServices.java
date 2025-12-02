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
import com.swifttrack.AuthService.Models.Enum.UserType;
import com.swifttrack.AuthService.Repository.RoleRespository;
import com.swifttrack.AuthService.Repository.UserRepo;
import com.swifttrack.AuthService.Repository.UserRolesRepo;
import com.swifttrack.AuthService.util.JwtUtil;
import com.swifttrack.dto.Message;
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
            System.out.println("Adding user roles...");
            // Get the user
            Map<String, Object> map = jwtUtil.decodeToken(token);
            System.out.println(map+"map");
            if(!map.containsKey("mobile")) throw new CustomException(HttpStatus.FORBIDDEN, "Forbidden");
            UserModel user = userRepo.findByMobile((String) map.get("mobile"));
            System.out.println(user+"user");
            if (user == null) {
                throw new ResourceNotFoundException("User not found");
            }
            System.out.println(user.getType()+"user type");
            if(!(user.getType() == UserType.SYSTEM_ADMIN || user.getType() == UserType.SYSTEM_USER || user.getType() == UserType.TENANT_ADMIN)) {
                throw new CustomException(HttpStatus.FORBIDDEN, "Forbidden - Only system admin, system user, and tenant admin can assign roles");
            }
            UserModel userToAssignRole = userRepo.findById(UUID.fromString(userRoleInput.userId().toString())).orElse(null);
            if (userToAssignRole == null) {
                throw new ResourceNotFoundException("User to assign role not found");
            }
            // Process each role in the input
            for (String roleId : userRoleInput.roleList()) {
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