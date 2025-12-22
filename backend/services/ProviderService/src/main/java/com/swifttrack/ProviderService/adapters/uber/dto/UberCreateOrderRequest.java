package com.swifttrack.ProviderService.adapters.uber.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UberCreateOrderRequest {

    @JsonProperty("dropoff_address")
    private String dropoffAddress;

    @JsonProperty("dropoff_name")
    private String dropoffName;

    @JsonProperty("dropoff_phone_number")
    private String dropoffPhoneNumber;

    @JsonProperty("manifest_items")
    private List<ManifestItem> manifestItems;

    @JsonProperty("pickup_address")
    private String pickupAddress;

    @JsonProperty("pickup_name")
    private String pickupName;

    @JsonProperty("pickup_phone_number")
    private String pickupPhoneNumber;

    @JsonProperty("pickup_business_name")
    private String pickupBusinessName;

    @JsonProperty("pickup_latitude")
    private Double pickupLatitude;

    @JsonProperty("pickup_longitude")
    private Double pickupLongitude;

    @JsonProperty("pickup_notes")
    private String pickupNotes;

    @JsonProperty("pickup_verification")
    private Verification pickupVerification;

    @JsonProperty("dropoff_business_name")
    private String dropoffBusinessName;

    @JsonProperty("dropoff_latitude")
    private Double dropoffLatitude;

    @JsonProperty("dropoff_longitude")
    private Double dropoffLongitude;

    @JsonProperty("dropoff_notes")
    private String dropoffNotes;

    @JsonProperty("dropoff_seller_notes")
    private String dropoffSellerNotes;

    @JsonProperty("dropoff_verification")
    private Verification dropoffVerification;

    @JsonProperty("deliverable_action")
    private String deliverableAction;

    @JsonProperty("manifest_reference")
    private String manifestReference;

    @JsonProperty("manifest_total_value")
    private Integer manifestTotalValue;

    @JsonProperty("quote_id")
    private String quoteId;

    @JsonProperty("undeliverable_action")
    private String undeliverableAction;

    @JsonProperty("pickup_ready_dt")
    private String pickupReadyDt;

    @JsonProperty("pickup_deadline_dt")
    private String pickupDeadlineDt;

    @JsonProperty("dropoff_ready_dt")
    private String dropoffReadyDt;

    @JsonProperty("dropoff_deadline_dt")
    private String dropoffDeadlineDt;

    @JsonProperty("requires_dropoff_signature")
    private Boolean requiresDropoffSignature;

    @JsonProperty("requires_id")
    private Boolean requiresId;

    @JsonProperty("tip")
    private Integer tip;

    @JsonProperty("idempotency_key")
    private String idempotencyKey;

    @JsonProperty("external_store_id")
    private String externalStoreId;

    @JsonProperty("return_verification")
    private Verification returnVerification;

    @JsonProperty("external_user_info")
    private ExternalUserInfo externalUserInfo;

    @JsonProperty("external_id")
    private String externalId;

    @JsonProperty("test_specifications")
    private TestSpecifications testSpecifications;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ManifestItem {
        @JsonProperty("name")
        private String name;
        @JsonProperty("quantity")
        private Integer quantity;
        @JsonProperty("size")
        private String size;
        @JsonProperty("dimensions")
        private Dimensions dimensions;
        @JsonProperty("price")
        private Integer price;
        @JsonProperty("must_be_upright")
        private Boolean mustBeUpright;
        @JsonProperty("weight")
        private Integer weight;
        @JsonProperty("vat_percentage")
        private Integer vatPercentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Dimensions {
        @JsonProperty("length")
        private Integer length;
        @JsonProperty("height")
        private Integer height;
        @JsonProperty("depth")
        private Integer depth;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Verification {
        @JsonProperty("signature")
        private Boolean signature;
        @JsonProperty("signature_requirement")
        private SignatureRequirement signatureRequirement;
        @JsonProperty("barcodes")
        private List<Barcode> barcodes;
        @JsonProperty("identification")
        private Identification identification;
        @JsonProperty("picture")
        private Boolean picture;
        @JsonProperty("pincode")
        private Pincode pincode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignatureRequirement {
        @JsonProperty("enabled")
        private Boolean enabled;
        @JsonProperty("collect_signer_name")
        private Boolean collectSignerName;
        @JsonProperty("collect_signer_relationship")
        private Boolean collectSignerRelationship;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Barcode {
        @JsonProperty("value")
        private String value;
        @JsonProperty("type")
        private String type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Identification {
        @JsonProperty("min_age")
        private Integer minAge;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pincode {
        @JsonProperty("enabled")
        private Boolean enabled;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExternalUserInfo {
        @JsonProperty("merchant_account")
        private MerchantAccount merchantAccount;
        @JsonProperty("device")
        private Device device;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MerchantAccount {
        @JsonProperty("account_created_at")
        private String accountCreatedAt;
        @JsonProperty("email")
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Device {
        @JsonProperty("id")
        private String id;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestSpecifications {
        @JsonProperty("robo_courier_specification")
        private RoboCourierSpecification roboCourierSpecification;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoboCourierSpecification {
        @JsonProperty("mode")
        private String mode;
    }
}
