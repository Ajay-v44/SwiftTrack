package com.swifttrack.AuthService.util;

import com.swifttrack.AuthService.Models.Roles;
import com.swifttrack.AuthService.Dto.RoleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    
    @Mapping(source = "name", target = "name", qualifiedByName = "roleNameToString")
    RoleResponse roleToRoleResponse(Roles role);
    
    List<RoleResponse> rolesToRoleResponses(List<Roles> roles);
    
    @Named("roleNameToString")
    default String roleNameToString(Roles.Name name) {
        return name != null ? name.name() : null;
    }
}