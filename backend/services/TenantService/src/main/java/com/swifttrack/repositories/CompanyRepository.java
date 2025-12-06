package com.swifttrack.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swifttrack.Models.TenantModel;

@Repository
public interface CompanyRepository extends JpaRepository<TenantModel, UUID> {

    TenantModel findByTenantCode(String tenantCode);

    TenantModel findByOrganizationName(String organizationName);

    TenantModel findByOrganizationEmail(String organizationEmail);

    TenantModel findByOrganizationPhone(String organizationPhone);

    TenantModel findByOrganizationWebsite(String organizationWebsite);

}
