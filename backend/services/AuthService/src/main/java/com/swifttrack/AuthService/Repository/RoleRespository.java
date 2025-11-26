package com.swifttrack.AuthService.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.swifttrack.AuthService.Models.Roles;

public interface RoleRespository  extends JpaRepository<Roles,UUID>{
   List<Roles> findByStatus(Boolean status);
}