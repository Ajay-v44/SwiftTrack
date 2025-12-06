package com.swifttrack.dto;

import com.swifttrack.enums.UserType;

public record AddTenantUsers(

        String name,
        String password,
        String email,
        String mobile,
        UserType userType,
        Boolean status) {

}
