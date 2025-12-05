package com.swifttrack.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swifttrack.dto.Message;
import com.swifttrack.dto.RegisterOrg;
import com.swifttrack.services.CompanyService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/company")
@Tag(name = "Company", description = "Company related operations")
public class CompanyController {
    private CompanyService companyService;
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }
    

    @PostMapping("/v1/register")
    public ResponseEntity<Message> registerCompany(@RequestParam String token,  @RequestBody RegisterOrg registerOrg){
        return ResponseEntity.ok(companyService.registerCompany(token,registerOrg));
    }

}
