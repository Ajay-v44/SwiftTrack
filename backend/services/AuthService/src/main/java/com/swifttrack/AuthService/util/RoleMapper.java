package com.swifttrack.AuthService.util;

import com.swifttrack.AuthService.Models.Roles;
import com.swifttrack.dto.adminDto.RoleViewResponse;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    
    RoleViewResponse roleToRoleResponse(Roles role);
    
    List<RoleViewResponse> rolesToRoleResponses(List<Roles> roles);
}
