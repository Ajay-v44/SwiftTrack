package com.swifttrack.OrderService.dto;

public record AddressRequest(
        String label,
        String line1,
        String line2,
        String city,
        String state,
        String country,
        String pincode,
        String locality,
        Double latitude,
        Double longitude,
        String contactName,
        String contactPhone,
        String businessName,
        String notes,
        Boolean isDefault) {
}
