import React, { useEffect, useState } from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { useSelector, useDispatch } from 'react-redux';
import { RootState, AppDispatch } from '../store/store';
import { getDriverDetails, logout, setToken } from '../store/authSlice';
import * as SecureStore from 'expo-secure-store';
import { View, ActivityIndicator, Image, Text, StyleSheet } from 'react-native';
import { Colors } from '../theme/colors';
import { registerForPushNotificationsAsync } from '../utils/notifications';

import LoginScreen from '../screens/LoginScreen';
import TabNavigator from './TabNavigator';
import OrderTrackingScreen from '../screens/OrderTrackingScreen';
import DevMapScreen from '../screens/DevMapScreen';
import PrivacyPolicyScreen from '../screens/PrivacyPolicyScreen';
import HelpCenterScreen from '../screens/HelpCenterScreen';
import WithdrawScreen from '../screens/WithdrawScreen';

const Stack = createNativeStackNavigator();

export default function RootNavigator() {
  const { token, loading, driver } = useSelector((state: RootState) => state.auth);
  const dispatch = useDispatch<AppDispatch>();
  const [isReady, setIsReady] = useState(false);
  const [registeredPushUserId, setRegisteredPushUserId] = useState<string | null>(null);

  useEffect(() => {
    const loadToken = async () => {
      try {
        const storedToken = await SecureStore.getItemAsync('userToken');
        if (storedToken) {
          dispatch(setToken(storedToken));
          await dispatch(getDriverDetails()).unwrap();
        }
      } catch (e) {
        console.error('Failed to load token', e);
        await dispatch(logout());
      } finally {
        setIsReady(true);
      }
    };
    loadToken();
  }, [dispatch]);

  useEffect(() => {
    if (!token) {
      setRegisteredPushUserId(null);
    }
  }, [token]);

  useEffect(() => {
    const userId = driver?.user?.id;
    if (!token || !userId || registeredPushUserId === userId) {
      return;
    }

    registerForPushNotificationsAsync({
      userId,
      tenantId: driver?.user?.tenantId,
    })
      .then(() => {
        setRegisteredPushUserId(userId);
      })
      .catch((error) => {
        console.error('Failed to register notifications:', error);
      });
  }, [driver?.user?.id, driver?.user?.tenantId, registeredPushUserId, token]);

  if (!isReady || (token && !driver && loading)) {
    return (
      <View style={splashStyles.container}>
        <Image
          source={require('../../assets/images/swifttrack_logo.png')}
          style={splashStyles.logo}
          resizeMode="contain"
        />
        <Text style={splashStyles.brand}>SwiftTrack</Text>
        <ActivityIndicator size="large" color={Colors.primary} style={{ marginTop: 20 }} />
        <Text style={splashStyles.tagline}>Delivering the future</Text>
      </View>
    );
  }

  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      {token ? (
        <>
          <Stack.Screen name="MainTabs" component={TabNavigator} />
          <Stack.Screen name="OrderTracking" component={OrderTrackingScreen} />
          <Stack.Screen name="DevMap" component={DevMapScreen} />
          <Stack.Screen name="PrivacyPolicy" component={PrivacyPolicyScreen} />
          <Stack.Screen name="HelpCenter" component={HelpCenterScreen} />
          <Stack.Screen name="Withdraw" component={WithdrawScreen} />
        </>
      ) : (
        <Stack.Screen name="Login" component={LoginScreen} />
      )}
    </Stack.Navigator>
  );
}

const splashStyles = StyleSheet.create({
  container: {
    flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: Colors.bgDark,
  },
  logo: { width: 80, height: 80, borderRadius: 20, marginBottom: 12 },
  brand: { fontSize: 28, fontWeight: '800', color: Colors.textPrimary, letterSpacing: -0.5 },
  tagline: { fontSize: 14, color: Colors.textMuted, marginTop: 12 },
});
