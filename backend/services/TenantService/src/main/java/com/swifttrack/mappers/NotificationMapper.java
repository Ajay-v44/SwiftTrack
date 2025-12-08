package com.swifttrack.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swifttrack.Models.NotificationPreferences;
import com.swifttrack.dto.NotificationPreferenceDto;

@Mapper(componentModel = "spring")
public abstract class NotificationMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    @Mapping(target = "emailConfig", source = "emailConfig", qualifiedByName = "jsonNodeToString")
    @Mapping(target = "smsConfig", source = "smsConfig", qualifiedByName = "jsonNodeToString")
    @Mapping(target = "whatsappConfig", source = "whatsappConfig", qualifiedByName = "jsonNodeToString")
    @Mapping(target = "webhookConfig", source = "webhookConfig", qualifiedByName = "jsonNodeToString")
    @Mapping(target = "pushConfig", source = "pushConfig", qualifiedByName = "jsonNodeToString")
    public abstract NotificationPreferences toNotificationPreference(NotificationPreferenceDto notificationPreferenceDto);
    
    @Mapping(target = "emailConfig", source = "emailConfig", qualifiedByName = "stringToJsonNode")
    @Mapping(target = "smsConfig", source = "smsConfig", qualifiedByName = "stringToJsonNode")
    @Mapping(target = "whatsappConfig", source = "whatsappConfig", qualifiedByName = "stringToJsonNode")
    @Mapping(target = "webhookConfig", source = "webhookConfig", qualifiedByName = "stringToJsonNode")
    @Mapping(target = "pushConfig", source = "pushConfig", qualifiedByName = "stringToJsonNode")
    public abstract NotificationPreferenceDto toNotificationPreferenceDto(NotificationPreferences notificationPreferences);

    @Named("jsonNodeToString")
    public String jsonNodeToString(JsonNode jsonNode) {
        if (jsonNode == null) {
            return "{}";
        }
        return jsonNode.toString();
    }

    @Named("stringToJsonNode")
    public JsonNode stringToJsonNode(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) {
            jsonString = "{}";
        }
        try {
            return objectMapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            // Return empty object node if parsing fails
            return objectMapper.createObjectNode();
        }
    }
}