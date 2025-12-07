package com.swifttrack.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.swifttrack.FeignClients.AuthInterface;
import com.swifttrack.Models.TenantModel;
import com.swifttrack.dto.AddTenantUsers;
import com.swifttrack.dto.Message;
import com.swifttrack.dto.RegisterOrg;
import com.swifttrack.exception.CustomException;
import com.swifttrack.mappers.CompanyMapper;
import com.swifttrack.repositories.CompanyRepository;

@Service
public class CompanyService {

    private CompanyRepository companyRepository;
    private CompanyMapper companyMapper;
    private AuthInterface authInterface;

    CompanyService(CompanyRepository companyRepository, CompanyMapper companyMapper, AuthInterface authInterface) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
        this.authInterface = authInterface;
    }

    public Message registerCompany(String token, RegisterOrg registerOrg) {
        // validation
        if (companyRepository.findByTenantCode(registerOrg.tenantCode()) != null
                || companyRepository.findByOrganizationName(registerOrg.organizationName()) != null
                || companyRepository.findByOrganizationEmail(registerOrg.organizationEmail()) != null
                || companyRepository.findByOrganizationPhone(registerOrg.organizationPhone()) != null
                || companyRepository.findByOrganizationWebsite(registerOrg.organizationWebsite()) != null)
            throw new CustomException(HttpStatus.BAD_REQUEST, "Company already exists");
        TenantModel company = companyRepository.save(companyMapper.toEntity(registerOrg));
        authInterface.assignAdmin(token, company.getId());

        return new Message("Company Registered Successfully");
    }

    public Message addTenantUsers(String token, List<AddTenantUsers> addTenantUsers) {
        authInterface.addTenantUsers(token, addTenantUsers);
        return new Message("Tenant Users Added Successfully");
    }

}
