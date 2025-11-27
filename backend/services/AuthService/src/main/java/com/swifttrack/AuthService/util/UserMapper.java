package com.swifttrack.AuthService.util;

import org.mapstruct.Mapper;

import com.swifttrack.AuthService.Dto.RegisterUser;
import com.swifttrack.AuthService.Models.UserModel;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserModel toEntity(RegisterUser registerUser);
}
