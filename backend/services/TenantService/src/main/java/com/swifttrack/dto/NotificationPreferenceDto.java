package com.swifttrack.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record NotificationPreferenceDto(
boolean emailNotificationsEnabled,
boolean smsNotificationsEnabled,
boolean whatsappNotificationsEnabled,
boolean webhookNotificationsEnabled,
boolean pushNotificationsEnabled,
JsonNode emailConfig,
JsonNode smsConfig,
JsonNode whatsappConfig,
JsonNode webhookConfig,
JsonNode pushConfig

) {

}
