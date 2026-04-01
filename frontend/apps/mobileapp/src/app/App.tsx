import React, { useEffect, useState } from 'react';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { Provider } from 'react-redux';
import { NavigationContainer } from '@react-navigation/native';
import { store } from '../store/store';
import RootNavigator from '../navigation/RootNavigator';
import { registerForPushNotificationsAsync } from '../utils/notifications';
import { StatusBar } from 'expo-status-bar';

export const App = () => {
  useEffect(() => {
    // Register for push notifications on app start
    registerForPushNotificationsAsync().then(token => {
       console.log('App initialized with token:', token);
    }).catch(err => {
       console.error('Failed to register notifications:', err);
    });
  }, []);

  return (
    <Provider store={store}>
      <SafeAreaProvider>
        <NavigationContainer>
          <StatusBar style="auto" />
          <RootNavigator />
        </NavigationContainer>
      </SafeAreaProvider>
    </Provider>
  );
};

export default App;
