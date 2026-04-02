import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import apiClient from '../api/client';
import * as SecureStore from 'expo-secure-store';

interface AuthState {
  user: any | null;
  token: string | null;
  loading: boolean;
  error: string | null;
}

const initialState: AuthState = {
  user: null,
  token: null,
  loading: false,
  error: null,
};

// Async thunks
export const login = createAsyncThunk('auth/login', async (credentials: any, { rejectWithValue }) => {
  try {
    const isEmail = credentials.email !== undefined;
    const url = isEmail
      ? '/DriverAuthService/api/driver/auth/v1/loginWithEmailAndPassword'
      : '/DriverAuthService/api/driver/auth/v1/loginWithMobileNumberAndOtp';

    // Using correct mapping based on common microservice structure
    const response = await apiClient.post(url, credentials);
    const { token, driver } = response.data; // Adjust based on actual API response

    await SecureStore.setItemAsync('userToken', token);
    return { token, user: driver };
  } catch (error: any) {
    return rejectWithValue(error.response?.data?.message || 'Login failed');
  }
});

export const register = createAsyncThunk('auth/register', async (data: any, { rejectWithValue }) => {
  try {
    const response = await apiClient.post('/DriverService/api/driver/v1/register', data);
    return response.data;
  } catch (error: any) {
    return rejectWithValue(error.response?.data?.message || 'Registration failed');
  }
});

export const getDriverDetails = createAsyncThunk('auth/getDriverDetails', async (_, { rejectWithValue }) => {
  try {
    const response = await apiClient.get('/DriverService/api/driver/v1/getDriverDetails');
    return response.data;
  } catch (error: any) {
    return rejectWithValue(error.response?.data?.message || 'Failed to fetch details');
  }
});

export const logout = createAsyncThunk('auth/logout', async () => {
  await SecureStore.deleteItemAsync('userToken');
  return null;
});

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    setToken: (state, action) => {
      state.token = action.payload;
    }
  },
  extraReducers: (builder) => {
    // Login
    builder.addCase(login.pending, (state) => {
      state.loading = true;
      state.error = null;
    });
    builder.addCase(login.fulfilled, (state, action) => {
      state.loading = false;
      state.token = action.payload.token;
      state.user = action.payload.user;
    });
    builder.addCase(login.rejected, (state, action) => {
      state.loading = false;
      state.error = action.payload as string;
    });
    // Register
    builder.addCase(register.pending, (state) => {
      state.loading = true;
      state.error = null;
    });
    builder.addCase(register.fulfilled, (state) => {
      state.loading = false;
    });
    builder.addCase(register.rejected, (state, action) => {
      state.loading = false;
      state.error = action.payload as string;
    });
    // Get Details
    builder.addCase(getDriverDetails.pending, (state) => {
      state.loading = true;
    });
    builder.addCase(getDriverDetails.fulfilled, (state, action) => {
      state.loading = false;
      state.user = action.payload;
    });
    builder.addCase(getDriverDetails.rejected, (state, action) => {
      state.loading = false;
      // Don't set error here, just fail silently or handle globally
    });
    // Logout
    builder.addCase(logout.fulfilled, (state) => {
      state.user = null;
      state.token = null;
    });
  },
});

export const { clearError, setToken } = authSlice.actions;
export default authSlice.reducer;
