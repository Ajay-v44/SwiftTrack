package com.swifttrack.BillingAndSettlementService.models.enums;

import com.swifttrack.exception.CustomException;
import org.springframework.http.HttpStatus;

public enum SelectedType {
    LOCAL_DRIVERS,
    TENANT_DRIVERS,
    EXTERNAL_PROVIDERS;

    public static SelectedType fromValue(String value) {
        if (value == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "selectedType is required");
        }
        try {
            return SelectedType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new CustomException(HttpStatus.BAD_REQUEST,
                    "selectedType must be one of LOCAL_DRIVERS, TENANT_DRIVERS, EXTERNAL_PROVIDERS");
        }
    }
}
