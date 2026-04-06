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
        if (deviceToken == null
                || deviceToken.getUserId() == null
                || deviceToken.getUserId().trim().isEmpty()
                || deviceToken.getToken() == null
                || deviceToken.getToken().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("userId and token are required");
        }

        try {
            firebaseService.registerToken(deviceToken);
            return ResponseEntity.ok("Token registered successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error registering token: " + e.getMessage());
        }
    }
}
