package com.swifttrack.AuthService.Dto;

import com.swifttrack.AuthService.Models.Roles;

public record CreateRole(Roles.Name name, String description) {

}
