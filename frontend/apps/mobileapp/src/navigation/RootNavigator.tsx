import React, { useEffect, useState } from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { useSelector, useDispatch } from 'react-redux';
import { RootState, AppDispatch } from '../store/store';
import { getDriverDetails, setToken } from '../store/authSlice';
import * as SecureStore from 'expo-secure-store';
import { View, ActivityIndicator } from 'react-native';

import LoginScreen from '../screens/LoginScreen';
import TabNavigator from './TabNavigator';
import OrderTrackingScreen from '../screens/OrderTrackingScreen';
import DevMapScreen from '../screens/DevMapScreen';

const Stack = createNativeStackNavigator();

export default function RootNavigator() {
  const { token, loading, user } = useSelector((state: RootState) => state.auth);
  const dispatch = useDispatch<AppDispatch>();
  const [isReady, setIsReady] = useState(false);

  useEffect(() => {
    const loadToken = async () => {
      try {
        const storedToken = await SecureStore.getItemAsync('userToken');
        if (storedToken) {
           dispatch(setToken(storedToken));
           // Re-fetch user details to make sure token is valid and data is fresh
           await dispatch(getDriverDetails()).unwrap();
        }
      } catch (e) {
        console.error("Failed to load token", e);
      } finally {
        setIsReady(true);
      }
    };
    loadToken();
  }, [dispatch]);

  if (!isReady || (token && !user && loading)) {
    return (
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
        <ActivityIndicator size="large" color="#2563EB" />
      </View>
    );
  }

  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      {token ? (
        // Authorized Stack
        <>
          <Stack.Screen name="MainTabs" component={TabNavigator} />
          <Stack.Screen name="OrderTracking" component={OrderTrackingScreen} />
          <Stack.Screen name="DevMap" component={DevMapScreen} />
        </>
      ) : (
        // Unauthorized Stack
        <Stack.Screen name="Login" component={LoginScreen} />
      )}
    </Stack.Navigator>
  );
}
