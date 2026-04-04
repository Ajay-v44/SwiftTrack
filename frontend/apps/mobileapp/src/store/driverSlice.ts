import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import apiClient from '../api/client';

interface DriverState {
  isActive: boolean;
  currentLocation: { lat: number; lng: number } | null;
  loading: boolean;
  error: string | null;
}

const initialState: DriverState = {
  isActive: false,
  currentLocation: null,
  loading: false,
  error: null,
};

export const updateStatus = createAsyncThunk('driver/updateStatus', async (status: string, { rejectWithValue }) => {
  try {
    await apiClient.post('/driverservice/api/driver/v1/updateStatus', { status });
    return status;
  } catch (error: any) {
    return rejectWithValue(error.response?.data?.message || 'Failed to update status');
  }
});

export const updateLocation = createAsyncThunk('driver/updateLocation', async (location: { lat: number; lng: number }, { rejectWithValue }) => {
  try {
    await apiClient.post('/driverservice/api/driver/v1/location', location);
    return location;
  } catch (error: any) {
    return rejectWithValue(error.response?.data?.message || 'Failed to update location');
  }
});

const driverSlice = createSlice({
  name: 'driver',
  initialState,
  reducers: {
    setIsActive: (state, action) => {
      state.isActive = action.payload === 'ACTIVE';
    },
    setCurrentLocation: (state, action) => {
      state.currentLocation = action.payload;
    }
  },
  extraReducers: (builder) => {
    builder.addCase(updateStatus.pending, (state) => {
      state.loading = true;
      state.error = null;
    });
    builder.addCase(updateStatus.fulfilled, (state, action) => {
      state.loading = false;
      state.isActive = action.payload === 'ACTIVE';
    });
    builder.addCase(updateStatus.rejected, (state, action) => {
      state.loading = false;
      state.error = action.payload as string;
    });
    builder.addCase(updateLocation.fulfilled, (state, action) => {
      state.currentLocation = action.payload;
    });
  },
});

export const { setIsActive, setCurrentLocation } = driverSlice.actions;
export default driverSlice.reducer;
