package com.swifttrack.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.swifttrack.FeignClients.AuthInterface;
import com.swifttrack.FeignClients.BillingAndSettlementInterface;
import com.swifttrack.Models.TenantModel;
import com.swifttrack.dto.AddTenantUsers;
import com.swifttrack.dto.Message;
import com.swifttrack.dto.RegisterOrg;
import com.swifttrack.enums.BillingAndSettlement.AccountType;
import com.swifttrack.exception.CustomException;
import com.swifttrack.mappers.CompanyMapper;
import com.swifttrack.repositories.CompanyRepository;

@Service
public class CompanyService {

    private CompanyRepository companyRepository;
    private CompanyMapper companyMapper;
    private AuthInterface authInterface;
    private BillingAndSettlementInterface billingAndSettlementInterface;

    CompanyService(CompanyRepository companyRepository, CompanyMapper companyMapper, AuthInterface authInterface,
            BillingAndSettlementInterface billingAndSettlementInterface) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
        this.authInterface = authInterface;
        this.billingAndSettlementInterface = billingAndSettlementInterface;
    }

    public Message registerCompany(String token, java.util.UUID id, RegisterOrg registerOrg) {
        // validation
        if (companyRepository.findByTenantCode(registerOrg.tenantCode()) != null ||
                companyRepository.findByOrganizationName(registerOrg.organizationName()) != null ||
                companyRepository.findByOrganizationEmail(registerOrg.organizationEmail()) != null ||
                companyRepository.findByOrganizationPhone(registerOrg.organizationPhone()) != null ||
                companyRepository.findByOrganizationWebsite(registerOrg.organizationWebsite()) != null)
            throw new CustomException(HttpStatus.BAD_REQUEST, "Company already exists");
        TenantModel company = new TenantModel();
        company.setId(id);
        company.setTenantCode(registerOrg.tenantCode());
        company.setOrganizationName(registerOrg.organizationName());
        company.setOrganizationEmail(registerOrg.organizationEmail());
        company.setOrganizationPhone(registerOrg.organizationPhone());
        company.setOrganizationWebsite(registerOrg.organizationWebsite());
        company.setOrganizationAddress(registerOrg.organizationAddress());
        company.setOrganizationCity(registerOrg.organizationCity());
        company.setOrganizationCountry(registerOrg.organizationCountry());
        company.setGstNumber(registerOrg.gstNumber());
        company.setCinNumber(registerOrg.cinNumber());
        company.setPanNumber(registerOrg.panNumber());
        company.setLogoUrl(registerOrg.logoUrl());
        company.setThemeColor(registerOrg.themeColor());
        companyRepository.save(company);
        billingAndSettlementInterface.createAccount(token, id, AccountType.TENANT);

        return new Message("Company Registered Successfully");
    }

    public Message addTenantUsers(String token, List<AddTenantUsers> addTenantUsers) {
        authInterface.addTenantUsers(token, addTenantUsers);
        return new Message("Tenant Users Added Successfully");
    }

}
