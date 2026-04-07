import axios from 'axios';
import * as SecureStore from 'expo-secure-store';
import { Platform } from 'react-native';

// Handle Android emulator localhost specifically
const getBaseUrl = () => {
  if (process.env.EXPO_PUBLIC_API_URL) {
    return process.env.EXPO_PUBLIC_API_URL;
  }
  if (Platform.OS === 'android') {
    return 'http://10.0.2.2:8080';
  }
  return 'http://127.0.0.1:8080';
};

const apiClient = axios.create({
  baseURL: getBaseUrl(),
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000, // 15 second timeout for production reliability
});

let store: any;

const isAuthFailureMessage = (message?: string) => {
  const normalizedMessage = message?.toLowerCase() ?? '';
  return (
    normalizedMessage.includes('expired token') ||
    normalizedMessage.includes('invalid token') ||
    normalizedMessage.includes('invalid or expired token') ||
    normalizedMessage.includes('missing auth token') ||
    normalizedMessage.includes('unauthorized') ||
    normalizedMessage.includes('[401]') ||
    normalizedMessage.includes('getuserdetails')
  );
};

export const injectStore = (_store: any) => {
  store = _store;
};

export const isAuthFailureError = (error: any) => {
  const status = error?.response?.status;
  const data = error?.response?.data;
  const message =
    typeof data?.message === 'string'
      ? data.message
      : typeof data?.error === 'string'
        ? data.error
        : typeof error?.message === 'string'
          ? error.message
          : '';

  return status === 401 || status === 403 || (status === 500 && isAuthFailureMessage(message));
};

// Request interceptor to add auth token
// IMPORTANT: Using 'token' header to match the dashboard/gateway convention
apiClient.interceptors.request.use(
  async (config) => {
    const token = await SecureStore.getItemAsync('userToken');
    if (token) {
      config.headers['token'] = token;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle 401s/403s globally
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (isAuthFailureError(error)) {
      // Clear token from secure storage
      await SecureStore.deleteItemAsync('userToken');
      // Clear Redux state to trigger navigation to Login screen
      if (store) {
        store.dispatch({ type: 'auth/logout/fulfilled' });
      }
    }

    return Promise.reject(error);
  }
);

export default apiClient;
