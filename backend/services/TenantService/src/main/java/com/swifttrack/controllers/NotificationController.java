package com.swifttrack.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.swifttrack.dto.NotificationPreferenceDto;
import com.swifttrack.services.NotificationService;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@RequestMapping("/notifications/")
@Tag(name = "Notifications 🔔", description = "Notification management")
public class NotificationController {
    private final NotificationService notificationService;
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    @GetMapping("v1/preferences")
    @Operation(summary = "Get notification preferences", description = "Get notification preferences for a tenant")
    @ApiResponse(responseCode = "200", description = "Notification preferences retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Notification preferences not found")
    public ResponseEntity<NotificationPreferenceDto> getNotificationPreferences(@RequestHeader String token) {
        return ResponseEntity.ok(notificationService.getNotificationPreferences(token));
    }

    @PutMapping("v1/preferences")
    @Operation(summary = "Update notification preferences", description = "Update notification preferences for a tenant")
    @ApiResponse(responseCode = "200", description = "Notification preferences updated successfully")
    @ApiResponse(responseCode = "404", description = "Notification preferences not found")
    public ResponseEntity<NotificationPreferenceDto> updateNotificationPreferences(@RequestHeader String token,@RequestBody NotificationPreferenceDto notificationPreferenceDto) {
        return ResponseEntity.ok(notificationService.updateNotificationPreferences(token, notificationPreferenceDto));
    }
}
