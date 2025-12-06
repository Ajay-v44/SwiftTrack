package com.swifttrack.mappers;

import org.mapstruct.Mapper;

import com.swifttrack.Models.TenantModel;
import com.swifttrack.dto.RegisterOrg;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    TenantModel toEntity(RegisterOrg registerOrg);

}
