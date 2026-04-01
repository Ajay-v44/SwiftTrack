import * as Location from 'expo-location';
import * as TaskManager from 'expo-task-manager';
import apiClient from '../api/client';

const LOCATION_TASK_NAME = 'background-location-task';

TaskManager.defineTask(LOCATION_TASK_NAME, async ({ data, error }) => {
  if (error) {
    console.error('Background location task error:', error);
    return;
  }
  if (data) {
    const { locations } = data as { locations: Location.LocationObject[] };
    const location = locations[0];

    if (location) {
      try {
        await apiClient.post('/DriverService/api/driver/v1/location', {
          lat: location.coords.latitude,
          lng: location.coords.longitude,
        });
      } catch (err) {
        console.error('Failed to update background location', err);
      }
    }
  }
});

export const startLocationTracking = async () => {
  const { status: foregroundStatus } = await Location.requestForegroundPermissionsAsync();
  if (foregroundStatus !== 'granted') {
    console.log('Permission to access foreground location was denied');
    return;
  }

  const { status: backgroundStatus } = await Location.requestBackgroundPermissionsAsync();
  if (backgroundStatus !== 'granted') {
    console.log('Permission to access background location was denied');
    return;
  }

  await Location.startLocationUpdatesAsync(LOCATION_TASK_NAME, {
    accuracy: Location.Accuracy.Balanced,
    // 2 minutes = 120000 milliseconds
    timeInterval: 120000,
    distanceInterval: 10,
    deferredUpdatesInterval: 120000,
    foregroundService: {
      notificationTitle: "SwiftTrack is tracking your location",
      notificationBody: "Your location is being updated in the background to ensure you get nearby orders.",
    }
  });
};

export const stopLocationTracking = async () => {
  const isRegistered = await TaskManager.isTaskRegisteredAsync(LOCATION_TASK_NAME);
  if (isRegistered) {
    await Location.stopLocationUpdatesAsync(LOCATION_TASK_NAME);
  }
};
