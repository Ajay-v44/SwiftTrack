import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import apiClient from '../api/client';
import { getDriverDetails, logout } from './authSlice';

/**
 * Backend DTOs:
 * - UpdateDriverStatusRequest { status: DriverOnlineStatus(OFFLINE|ONLINE|ON_TRIP|SUSPENDED) }
 * - DriverLocationUpdateDto { latitude: BigDecimal, longitude: BigDecimal }
 * Both endpoints require @RequestHeader String token (sent automatically by interceptor)
 */

export interface DriverState {
  isOnline: boolean;
  currentLocation: { latitude: number; longitude: number } | null;
  loading: boolean;
  error: string | null;
}

const initialState: DriverState = {
  isOnline: false,
  currentLocation: null,
  loading: false,
  error: null,
};

export const updateStatus = createAsyncThunk(
  'driver/updateStatus',
  async (status: 'ONLINE' | 'OFFLINE' | 'ON_TRIP' | 'SUSPENDED', { rejectWithValue }) => {
    try {
      // POST /api/driver/v1/updateStatus — body: { status: DriverOnlineStatus }
      await apiClient.post('/driverservice/api/driver/v1/updateStatus', { status });
      return status;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to update status');
    }
  }
);

export const updateLocation = createAsyncThunk(
  'driver/updateLocation',
  async (location: { latitude: number; longitude: number }, { rejectWithValue }) => {
    try {
      // POST /api/driver/v1/location — body: { latitude, longitude } (BigDecimal)
      await apiClient.post('/driverservice/api/driver/v1/location', location);
      return location;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to update location');
    }
  }
);

const driverSlice = createSlice({
  name: 'driver',
  initialState,
  reducers: {
    setOnlineStatus: (state, action) => {
      state.isOnline = action.payload === 'ONLINE';
    },
    setCurrentLocation: (state, action) => {
      state.currentLocation = action.payload;
    },
  },
  extraReducers: (builder) => {
    builder.addCase(getDriverDetails.fulfilled, (state, action) => {
      state.isOnline = action.payload.status === 'ONLINE' || action.payload.status === 'ON_TRIP';
    });
    builder.addCase(updateStatus.pending, (state) => {
      state.loading = true;
      state.error = null;
    });
    builder.addCase(updateStatus.fulfilled, (state, action) => {
      state.loading = false;
      state.isOnline = action.payload === 'ONLINE';
    });
    builder.addCase(updateStatus.rejected, (state, action) => {
      state.loading = false;
      state.error = action.payload as string;
    });
    builder.addCase(updateLocation.fulfilled, (state, action) => {
      state.currentLocation = action.payload;
    });
    builder.addCase(logout.fulfilled, () => initialState);
  },
});

export const { setOnlineStatus, setCurrentLocation } = driverSlice.actions;
export default driverSlice.reducer;
