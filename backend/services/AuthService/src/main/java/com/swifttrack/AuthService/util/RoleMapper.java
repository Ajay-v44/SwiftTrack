package com.swifttrack.AuthService.util;

import com.swifttrack.AuthService.Models.Roles;
import com.swifttrack.AuthService.Dto.RoleResponse;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    
    RoleResponse roleToRoleResponse(Roles role);
    
    List<RoleResponse> rolesToRoleResponses(List<Roles> roles);
}