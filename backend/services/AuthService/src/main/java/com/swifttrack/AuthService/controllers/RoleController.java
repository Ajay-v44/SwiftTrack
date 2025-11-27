package com.swifttrack.AuthService.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swifttrack.AuthService.Dto.CreateRole;
import com.swifttrack.AuthService.Dto.RoleResponse;
import com.swifttrack.AuthService.Services.RoleServices;

@RestController
@RequestMapping("/user-role")
public class RoleController {

    @Autowired
    RoleServices userRoleServices;
    
    @GetMapping("/v1")
    public ResponseEntity<List<RoleResponse>> getUserRoles(@RequestParam Boolean status){
        try{
            return ResponseEntity.ok(userRoleServices.getRoles(status));
        }catch(Exception e){
            return ResponseEntity.badRequest().build();
        }
    }
    @PostMapping("/v1")
    public ResponseEntity<String> createRole(@RequestBody CreateRole createRole){
        try{
            return ResponseEntity.ok(userRoleServices.createRole(createRole));
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

}