import { configureStore } from '@reduxjs/toolkit';

// Import slices here as they are created
import authReducer from './authSlice';
import driverReducer from './driverSlice';
import ordersReducer from './ordersSlice';
import walletReducer from './walletSlice';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    driver: driverReducer,
    orders: ordersReducer,
    wallet: walletReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: false, // Sometimes needed for Expo/React Navigation complex states
    }),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
