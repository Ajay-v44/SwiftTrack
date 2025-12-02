package com.swifttrack.AuthService.util;

import java.util.Optional;

import org.mapstruct.Mapper;

import com.swifttrack.AuthService.Dto.RegisterUser;
import com.swifttrack.AuthService.Models.UserModel;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserModel toEntity(RegisterUser registerUser);

    default <T> Optional<T> map(T value) {
        return Optional.ofNullable(value);
    }
}
