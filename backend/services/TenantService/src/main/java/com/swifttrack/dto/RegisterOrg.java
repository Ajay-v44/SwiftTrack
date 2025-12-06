package com.swifttrack.dto;

public record RegisterOrg(
                String tenantCode,
                String organizationName,
                String organizationEmail,
                String organizationPhone,
                String organizationAddress,
                String organizationWebsite,
                String organizationState,
                String organizationCity,
                String organizationCountry,
                String gstNumber,
                String cinNumber,
                String panNumber,
                String logoUrl,
                String themeColor) {

}
