package com.swifttrack.AuthService.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swifttrack.AuthService.Dto.UserRoleInput;
import com.swifttrack.AuthService.Services.UserRoleServices;
import com.swifttrack.dto.Message;

@RestController
@RequestMapping("/api/assignRole")
public class UserRoleController {

    @Autowired
    UserRoleServices userRoleServices;
    
    @PostMapping("/v1/add")
    public ResponseEntity<Message> addUserRole(@RequestParam String token, @RequestBody UserRoleInput input) {
        return ResponseEntity.ok(userRoleServices.addUserRoles(token, input));
    }
}
