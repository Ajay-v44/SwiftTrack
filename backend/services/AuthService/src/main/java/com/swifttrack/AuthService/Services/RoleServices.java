package com.swifttrack.AuthService.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.swifttrack.AuthService.Dto.CreateRole;
import com.swifttrack.AuthService.Models.Roles;
import com.swifttrack.AuthService.Repository.RoleRespository;
import com.swifttrack.AuthService.util.RoleMapper;
import com.swifttrack.dto.adminDto.RoleViewResponse;
import com.swifttrack.exception.CustomException;

@Service
public class RoleServices {

    @Autowired
    RoleRespository roleRepo;
    @Autowired
    RoleMapper roleMapper;
    

    public List<RoleViewResponse> getRoles(Boolean status) {
        List<Roles> roles=roleRepo.findByStatus(status);
        return roleMapper.rolesToRoleResponses(roles);
    }

    public String createRole(CreateRole createRole) {
        if (createRole == null || createRole.name() == null || createRole.name().trim().isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Role name is required");
        }
        if (roleRepo.findByNameIgnoreCase(createRole.name().trim()) != null) {
            throw new CustomException(HttpStatus.CONFLICT, "Role already exists");
        }
        Roles role = new Roles();
        role.setName(createRole.name().trim());
        role.setDescription(createRole.description());
        role.setStatus(true);
        roleRepo.save(role);
        return "Role created";
    }
    
}
