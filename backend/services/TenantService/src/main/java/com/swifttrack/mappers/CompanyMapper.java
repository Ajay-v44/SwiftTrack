package com.swifttrack.mappers;

import org.mapstruct.Mapper;

import com.swifttrack.Models.CompanyModel;
import com.swifttrack.dto.RegisterOrg;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    CompanyModel toEntity(RegisterOrg registerOrg);

}
