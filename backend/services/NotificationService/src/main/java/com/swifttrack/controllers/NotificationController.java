package com.swifttrack.controllers;

import com.swifttrack.dto.DeviceToken;
import com.swifttrack.services.notification.FirebaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification APIs", description = "Endpoints for managing push notification tokens")
public class NotificationController {

    private final FirebaseService firebaseService;

    public NotificationController(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    @PostMapping("/token")
    @Operation(summary = "Register Device Token", description = "Registers an FCM device token in Firestore for targeted push notifications")
    public ResponseEntity<String> registerToken(@RequestBody DeviceToken deviceToken) {
        try {
            firebaseService.registerToken(deviceToken);
            String topic = getTenantTopic(deviceToken);
            if (topic != null) {
                firebaseService.subscribeToTopic(topic, deviceToken.getToken());
            }
            return ResponseEntity.ok("Token registered successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error registering token: " + e.getMessage());
        }
    }

    private String getTenantTopic(DeviceToken token) {
        if (token.getTenantId() != null && !token.getTenantId().trim().isEmpty()) {
            return "tenant_" + token.getTenantId();
        }
        return null;
    }
}
