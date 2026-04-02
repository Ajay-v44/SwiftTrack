import * as Device from 'expo-device';
import * as Notifications from 'expo-notifications';
import { Platform } from 'react-native';
import apiClient from '../api/client';

Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: true,
    shouldPlaySound: true,
    shouldSetBadge: false,
  }),
});

export async function registerForPushNotificationsAsync() {
  let token;

  if (Platform.OS === 'android') {
    await Notifications.setNotificationChannelAsync('default', {
      name: 'default',
      importance: Notifications.AndroidImportance.MAX,
      vibrationPattern: [0, 250, 250, 250],
      lightColor: '#FF231F7C',
    });
  }

  if (Device.isDevice) {
    const { status: existingStatus } = await Notifications.getPermissionsAsync();
    let finalStatus = existingStatus;
    if (existingStatus !== 'granted') {
      const { status } = await Notifications.requestPermissionsAsync();
      finalStatus = status;
    }
    if (finalStatus !== 'granted') {
      console.log('Failed to get push token for push notification!');
      return;
    }

    // Project ID should match the EAS project ID or be explicitly configured
    token = (await Notifications.getExpoPushTokenAsync({
       projectId: process.env.EXPO_PUBLIC_PROJECT_ID || 'your-project-id'
    })).data;
    console.log('Expo Push Token:', token);

    // Call backend API to register this token for the driver
    // Example endpoint based on standard setup
    try {
       await apiClient.post('/NotificationService/api/notifications/register-token', { token, deviceType: Platform.OS });
    } catch(err) {
       console.log("Failed to register push token with backend", err);
    }
  } else {
    console.log('Must use physical device for Push Notifications');
  }

  return token;
}
