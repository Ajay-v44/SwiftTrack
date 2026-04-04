import React, { useEffect, useState } from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { useSelector, useDispatch } from 'react-redux';
import { RootState, AppDispatch } from '../store/store';
import { getDriverDetails, setToken } from '../store/authSlice';
import * as SecureStore from 'expo-secure-store';
import { View, ActivityIndicator } from 'react-native';
import { Colors } from '../theme/colors';

import LoginScreen from '../screens/LoginScreen';
import TabNavigator from './TabNavigator';
import OrderTrackingScreen from '../screens/OrderTrackingScreen';
import DevMapScreen from '../screens/DevMapScreen';

const Stack = createNativeStackNavigator();

export default function RootNavigator() {
  const { token, loading, driver } = useSelector((state: RootState) => state.auth);
  const dispatch = useDispatch<AppDispatch>();
  const [isReady, setIsReady] = useState(false);

  useEffect(() => {
    const loadToken = async () => {
      try {
        const storedToken = await SecureStore.getItemAsync('userToken');
        if (storedToken) {
           dispatch(setToken(storedToken));
           // Re-fetch driver details to make sure token is valid and data is fresh
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

  if (!isReady || (token && !driver && loading)) {
    return (
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: Colors.bgDark }}>
        <ActivityIndicator size="large" color={Colors.primary} />
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
