import * as Device from 'expo-device';
import * as Notifications from 'expo-notifications';
import { Platform } from 'react-native';
import apiClient from '../api/client';

const DISPATCH_NOTIFICATION_SOUND = 'ready_for_dispatch.mp3';

Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: true,
    shouldPlaySound: true,
    shouldSetBadge: true,
    shouldShowBanner: true,
    shouldShowList: true,
  }),
});

type PushRegistrationParams = {
  userId: string;
  tenantId?: string;
};

export async function registerForPushNotificationsAsync({
  userId,
  tenantId,
}: PushRegistrationParams) {
  if (!userId) {
    return;
  }

  if (Platform.OS === 'android') {
    await Notifications.setNotificationChannelAsync('default', {
      name: 'Default',
      importance: Notifications.AndroidImportance.MAX,
      vibrationPattern: [0, 250, 250, 250],
      lightColor: '#6C63FF',
      sound: DISPATCH_NOTIFICATION_SOUND,
    });
  }

  if (!Device.isDevice) {
    console.log('Must use physical device for push notifications');
    return;
  }

  const { status: existingStatus } = await Notifications.getPermissionsAsync();
  let finalStatus = existingStatus;
  if (existingStatus !== 'granted') {
    const { status } = await Notifications.requestPermissionsAsync();
    finalStatus = status;
  }

  if (finalStatus !== 'granted') {
    console.log('Push notification permission denied');
    return;
  }

  let nativePushToken: string | undefined;

  try {
    const devicePushToken = await Notifications.getDevicePushTokenAsync();
    nativePushToken =
      typeof devicePushToken.data === 'string'
        ? devicePushToken.data
        : devicePushToken.data
          ? JSON.stringify(devicePushToken.data)
          : undefined;
  } catch (error) {
    console.log('Failed to fetch native push token', error);
    return;
  }

  if (!nativePushToken) {
    console.log('Native push token unavailable');
    return;
  }

  try {
    await apiClient.post('/notificationservice/api/notifications/token', {
      userId,
      tenantId,
      token: nativePushToken,
    });
    console.log('Push token registered with backend');
  } catch (err) {
    console.log('Failed to register push token with backend', err);
    throw err;
  }

  return nativePushToken;
}

export function addNotificationReceivedListener(
  callback: (notification: Notifications.Notification) => void
) {
  return Notifications.addNotificationReceivedListener(callback);
}

export function addNotificationResponseReceivedListener(
  callback: (response: Notifications.NotificationResponse) => void
) {
  return Notifications.addNotificationResponseReceivedListener(callback);
}
