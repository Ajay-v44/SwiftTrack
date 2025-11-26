package com.swifttrack.AuthService.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swifttrack.AuthService.Dto.CreateRole;
import com.swifttrack.AuthService.Dto.RoleResponse;
import com.swifttrack.AuthService.Models.Roles;
import com.swifttrack.AuthService.Repository.RoleRespository;
import com.swifttrack.AuthService.util.RoleMapper;

@Service
public class RoleServices {

    @Autowired
    RoleRespository roleRepo;
    @Autowired
    RoleMapper roleMapper;
    

    public List<RoleResponse> getRoles(Boolean status) {
        List<Roles> roles=roleRepo.findByStatus(status);
        return roleMapper.rolesToRoleResponses(roles);
    }

    public String createRole(CreateRole createRole) {
        Roles role = new Roles();
        role.setName(createRole.name());
        role.setDescription(createRole.description());
        role.setStatus(true);
        roleRepo.save(role);
        return "Role created";
    }
    
}
