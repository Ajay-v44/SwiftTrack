package com.swifttrack.dto.orderDto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.swifttrack.enums.PaymentType;

public record CreateOrderRequest(

                // ---- Core Identifiers ----
                String idempotencyKey,
                String tenantId,
                String quoteId,
                String orderReference,
                OrderType orderType,
                PaymentType paymentType,

                // ---- Locations ----
                LocationPoint pickup,
                LocationPoint dropoff,

                // ---- Package / Items ----
                List<Item> items,
                PackageInfo packageInfo,

                // ---- Time & Preferences ----
                TimeWindows timeWindows,
                DeliveryPreferences deliveryPreferences,

                // ---- External / Provider Metadata ----
                ExternalMetadata externalMetadata,

                String deliveryInstructions

) {

        /*
         * =========================
         * Location & Contact
         * =========================
         */

        public record LocationPoint(
                        Address address,
                        Contact contact,
                        String businessName,
                        String notes,
                        VerificationRequirements verification) {
        }

        public record Address(
                        String line1,
                        String line2,
                        double latitude,
                        double longitude) {
        }

        public record Contact(
                        String name,
                        String phone) {
        }

        /*
         * =========================
         * Verification
         * =========================
         */

        public record VerificationRequirements(
                        boolean requireSignature,
                        boolean requireId,
                        Integer minAge,
                        boolean requirePhoto,
                        boolean requirePincode) {
        }

        /*
         * =========================
         * Items (Rich Providers)
         * =========================
         */

        public record Item(
                        String name,
                        int quantity,
                        Integer price,
                        Integer weightGrams,
                        Dimensions dimensionsCm,
                        boolean fragile,
                        boolean mustBeUpright) {
        }

        public record Dimensions(
                        int length,
                        int width,
                        int height) {
        }

        /*
         * =========================
         * Package (Simple Providers)
         * =========================
         */

        public record PackageInfo(
                        Integer totalValue,
                        Integer totalWeightGrams,
                        PackageSize size,
                        String description) {
        }

        /*
         * =========================
         * Time Windows
         * =========================
         */

        public record TimeWindows(
                        Instant pickupReadyAt,
                        Instant pickupDeadlineAt,
                        Instant dropoffReadyAt,
                        Instant dropoffDeadlineAt) {
        }

        /*
         * =========================
         * Delivery Preferences
         * =========================
         */

        public record DeliveryPreferences(
                        DeliverableAction deliverableAction,
                        UndeliverableAction undeliverableAction,
                        Integer tipAmount,
                        VehiclePreference vehiclePreference,
                        Priority priority) {
        }

        /*
         * =========================
         * External / Provider Metadata
         * =========================
         */

        public record ExternalMetadata(
                        String externalOrderId,
                        String storeId,
                        Map<String, String> providerOverrides,
                        UserContext userContext) {
        }

        public record UserContext(
                        String deviceId,
                        String userEmail) {
        }

        /*
         * =========================
         * Enums
         * =========================
         */

        public enum OrderType {
                ON_DEMAND,
                SCHEDULED,
                RETURN
        }

        public enum PackageSize {
                SMALL,
                MEDIUM,
                LARGE,
                XL
        }

        public enum DeliverableAction {
                MEET_AT_DOOR,
                LEAVE_AT_DOOR
        }

        public enum UndeliverableAction {
                RETURN,
                LEAVE_AT_LOCATION
        }

        public enum VehiclePreference {
                BIKE,
                CAR,
                VAN,
                ANY
        }

        public enum Priority {
                COST,
                SPEED,
                SAFETY
        }
}
