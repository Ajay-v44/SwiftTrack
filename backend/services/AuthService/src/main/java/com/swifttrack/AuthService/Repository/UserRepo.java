package com.swifttrack.AuthService.Repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.swifttrack.AuthService.Models.UserModel;

public interface UserRepo extends JpaRepository<UserModel,UUID> {
    UserModel findByName(String username);
    UserModel findByEmail(String email);
    UserModel findByMobile(String mobile);
}