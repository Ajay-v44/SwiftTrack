package com.swifttrack.services.notification;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.swifttrack.dto.DeviceToken;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class FirebaseService {

    private static final String COLLECTION_NAME = "device_tokens";

    public void registerToken(DeviceToken deviceToken) throws ExecutionException, InterruptedException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("token", deviceToken.getToken());
        if (deviceToken.getTenantId() != null) {
            tokenData.put("tenantId", deviceToken.getTenantId());
        }

        // We use userId as the document ID for simplicity, assuming one token per user.
        // If a user has multiple devices, we would use a sub-collection or array of tokens.
        // Considering "when user token changes update in fire store", one token per user is standard here.
        dbFirestore.collection(COLLECTION_NAME).document(deviceToken.getUserId())
                .set(tokenData, SetOptions.merge()).get();
        System.out.println("Registered token for user " + deviceToken.getUserId());

        // Automatically subscribe this device token to their specific tenant topic
        if (deviceToken.getTenantId() != null && !deviceToken.getTenantId().isEmpty()) {
            subscribeToTopic("tenant_" + deviceToken.getTenantId(), deviceToken.getToken());
        }
    }

    public void sendNotificationToUser(String userId, String title, String body) {
        try {
            Firestore dbFirestore = FirestoreClient.getFirestore();
            var docRef = dbFirestore.collection(COLLECTION_NAME).document(userId).get().get();
            if (docRef.exists() && docRef.contains("token")) {
                String token = docRef.getString("token");
                sendNotificationToToken(token, title, body);
            }
        } catch (Exception e) {
            System.err.println("Error sending notification to user " + userId + ": " + e.getMessage());
        }
    }

    public void sendNotificationToToken(String token, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder()
                                    .setChannelId("dispatch_alerts")
                                    .setSound("ready_for_dispatch.mp3")
                                    .build())
                            .build())
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder()
                                    .setSound("ready_for_dispatch.mp3")
                                    .build())
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Successfully sent message to token: " + response);
        } catch (Exception e) {
            System.err.println("Error sending direct notification " + e.getMessage());
        }
    }

    public void subscribeToTopic(String topic, String token) {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic(java.util.Collections.singletonList(token), topic);
        } catch (Exception e) {
            System.err.println("Error subscribing to topic " + e.getMessage());
        }
    }

    public void sendNotificationToTopic(String topic, String title, String body) {
        try {
            Message message = Message.builder()
                    .setTopic(topic)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder()
                                    .setChannelId("dispatch_alerts")
                                    .setSound("ready_for_dispatch.mp3")
                                    .build())
                            .build())
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder()
                                    .setSound("ready_for_dispatch.mp3")
                                    .build())
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Successfully sent message to topic " + topic + ": " + response);
        } catch (Exception e) {
            System.err.println("Error sending topic notification: " + e.getMessage());
        }
    }
}
