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
});

let store: any;

export const injectStore = (_store: any) => {
  store = _store;
};

// Request interceptor to add auth token
apiClient.interceptors.request.use(
  async (config) => {
    const token = await SecureStore.getItemAsync('userToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle 401s globally
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response && error.response.status === 401) {
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
