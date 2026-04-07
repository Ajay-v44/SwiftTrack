import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import apiClient from '../api/client';
import * as SecureStore from 'expo-secure-store';
import { isAuthFailureError } from '../api/client';

/** 
 * Backend DTOs Reference:
 * - LoginUser { email, password }
 * - LoginResponse { tokenType, accessToken }
 * - GetDriverUserDetails { user: TokenResponse, vehicleType, vehicleNumber, driverLicenseNumber, status }
 * - TokenResponse { id, tenantId?, providerId?, userType?, name, mobile, roles }
 */

export interface DriverUser {
  id: string;
  name: string;
  mobile: string;
  roles: string[];
  tenantId?: string;
  providerId?: string;
  userType?: string;
}

export interface DriverDetails {
  user: DriverUser;
  vehicleType: string | null;
  vehicleNumber: string | null;
  driverLicenseNumber: string | null;
  status: string; // DriverOnlineStatus: OFFLINE | ONLINE | ON_TRIP | SUSPENDED
}

export interface AuthState {
  driver: DriverDetails | null;
  token: string | null;
  loading: boolean;
  error: string | null;
}

const initialState: AuthState = {
  driver: null,
  token: null,
  loading: false,
  error: null,
};

export const login = createAsyncThunk('auth/login', async (credentials: any, { rejectWithValue }) => {
  try {
    const isEmail = credentials.email !== undefined;
    const url = isEmail
      ? '/driverservice/api/driver/auth/v1/loginWithEmailAndPassword'
      : '/driverservice/api/driver/auth/v1/loginWithMobileNumberAndOtp';

    const payload = isEmail
      ? { email: credentials.email.trim(), password: credentials.password }
      : { mobileNum: credentials.mobileNumber, otp: credentials.otp };

    const response = await apiClient.post(url, payload);
    const { accessToken } = response.data;

    if (!accessToken) {
      return rejectWithValue('No access token received from server');
    }

    await SecureStore.setItemAsync('userToken', accessToken);
    return { token: accessToken };
  } catch (error: any) {
    const data = error.response?.data;
    const message =
      data?.message ||
      data?.error ||
      (error.response?.status === 404 ? 'Account does not exist' : 'Login failed. Please try again.');
    return rejectWithValue(message);
  }
});

export const getDriverDetails = createAsyncThunk('auth/getDriverDetails', async (_, { rejectWithValue, dispatch }) => {
  try {
    // GET /api/driver/v1/getDriverDetails  — requires 'token' header (auto-sent by interceptor)
    const response = await apiClient.get('/driverservice/api/driver/v1/getDriverDetails');
    // Response: GetDriverUserDetails { user: TokenResponse, vehicleType, vehicleNumber, driverLicenseNumber, status }
    return response.data as DriverDetails;
  } catch (error: any) {
    if (isAuthFailureError(error)) {
      await dispatch(logout());
      return rejectWithValue('Session expired. Please login again.');
    }

    return rejectWithValue(error.response?.data?.message || 'Failed to fetch driver details');
  }
});

export const register = createAsyncThunk('auth/register', async (data: any, { rejectWithValue }) => {
  try {
    const response = await apiClient.post('/driverservice/api/driver/v1/register', {
      name: data.name,
      email: data.email.trim(),
      mobileNumber: data.mobileNumber,
      password: data.password,
    });
    return response.data;
  } catch (error: any) {
    return rejectWithValue(error.response?.data?.message || 'Registration failed');
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
    },
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
    });
    builder.addCase(login.rejected, (state, action) => {
      state.loading = false;
      state.error = action.payload as string;
    });
    // Get Driver Details
    builder.addCase(getDriverDetails.pending, (state) => {
      state.loading = true;
      state.error = null;
    });
    builder.addCase(getDriverDetails.fulfilled, (state, action) => {
      state.loading = false;
      state.driver = action.payload;
    });
    builder.addCase(getDriverDetails.rejected, (state, action) => {
      state.loading = false;
      state.driver = null;
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
    // Logout
    builder.addCase(logout.fulfilled, (state) => {
      state.driver = null;
      state.token = null;
      state.loading = false;
      state.error = null;
    });
  },
});

export const { clearError, setToken } = authSlice.actions;
export default authSlice.reducer;
