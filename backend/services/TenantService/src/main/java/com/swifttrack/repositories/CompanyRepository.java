package com.swifttrack.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swifttrack.Models.CompanyModel;

@Repository
public interface CompanyRepository  extends JpaRepository<CompanyModel, UUID> {

    CompanyModel findByOrganizationName(String organizationName);
    CompanyModel findByOrganizationEmail(String organizationEmail);
    CompanyModel findByOrganizationPhone(String organizationPhone);
    CompanyModel findByOrganizationWebsite(String organizationWebsite);

}
