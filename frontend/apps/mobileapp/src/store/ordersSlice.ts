import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import apiClient from '../api/client';

/**
 * Backend DTOs:
 * - GetOrdersForDriver { id: UUID, customerReferenceId, orderStatus, city, state, pickupLat, pickupLng, dropoffLat, dropoffLng }
 * - GET /api/driver/v1/getMyOrders?status=PENDING|ASSIGNED|ACCEPTED|REJECTED|CANCELLED|COMPLETED&page=0&limit=10
 * - RespondToAssignmentDto { orderId: UUID, accept: boolean, reason: String }
 * - UpdateOrderStatusrequest { status: TrackingStatus(PICKED_UP|IN_TRANSIT|OUT_FOR_DELIVERY|DELIVERED|FAILED), orderId: UUID }
 */

export interface DriverOrder {
  id: string;
  customerReferenceId: string;
  orderStatus: string;
  city: string;
  state: string;
  pickupLat: number;
  pickupLng: number;
  dropoffLat: number;
  dropoffLng: number;
}

export interface OrdersState {
  pendingOrders: DriverOrder[];
  acceptedOrders: DriverOrder[];
  completedOrders: DriverOrder[];
  loading: boolean;
  error: string | null;
}

const initialState: OrdersState = {
  pendingOrders: [],
  acceptedOrders: [],
  completedOrders: [],
  loading: false,
  error: null,
};

// Fetch pending/assigned orders
export const getPendingOrders = createAsyncThunk('orders/getPending', async (_, { rejectWithValue }) => {
  try {
    const response = await apiClient.get('/driverservice/api/driver/v1/getMyOrders', {
      params: { status: 'ASSIGNED', page: 0, limit: 20 },
    });
    return response.data as DriverOrder[];
  } catch (error: any) {
    return rejectWithValue(error.response?.data?.message || 'Failed to fetch pending orders');
  }
});

// Fetch accepted/active orders
export const getAcceptedOrders = createAsyncThunk('orders/getAccepted', async (_, { rejectWithValue }) => {
  try {
    const response = await apiClient.get('/driverservice/api/driver/v1/getMyOrders', {
      params: { status: 'ACCEPTED', page: 0, limit: 20 },
    });
    return response.data as DriverOrder[];
  } catch (error: any) {
    return rejectWithValue(error.response?.data?.message || 'Failed to fetch active orders');
  }
});

// Fetch completed orders
export const getCompletedOrders = createAsyncThunk('orders/getCompleted', async (_, { rejectWithValue }) => {
  try {
    const response = await apiClient.get('/driverservice/api/driver/v1/getMyOrders', {
      params: { status: 'COMPLETED', page: 0, limit: 20 },
    });
    return response.data as DriverOrder[];
  } catch (error: any) {
    return rejectWithValue(error.response?.data?.message || 'Failed to fetch completed orders');
  }
});

// Respond to order assignment — Backend expects: { orderId: UUID, accept: boolean, reason: String }
export const respondToAssignment = createAsyncThunk(
  'orders/respondAssignment',
  async ({ orderId, accept, reason }: { orderId: string; accept: boolean; reason?: string }, { rejectWithValue }) => {
    try {
      await apiClient.post('/driverservice/api/driver/v1/respond-assignment', {
        orderId,
        accept,
        reason: reason || null,
      });
      return { orderId, accept };
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to respond to assignment');
    }
  }
);

// Update order tracking status — Backend expects: { status: TrackingStatus, orderId: UUID }
export const updateOrderStatus = createAsyncThunk(
  'orders/updateStatus',
  async (
    { orderId, status }: { orderId: string; status: 'PICKED_UP' | 'IN_TRANSIT' | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'FAILED' },
    { rejectWithValue }
  ) => {
    try {
      await apiClient.post('/driverservice/api/driver/v1/updateOrderStatus', { orderId, status });
      return { orderId, status };
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to update order status');
    }
  }
);

const ordersSlice = createSlice({
  name: 'orders',
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    // Pending
    builder.addCase(getPendingOrders.pending, (state) => {
      state.loading = true;
      state.error = null;
    });
    builder.addCase(getPendingOrders.fulfilled, (state, action) => {
      state.loading = false;
      state.pendingOrders = action.payload;
    });
    builder.addCase(getPendingOrders.rejected, (state, action) => {
      state.loading = false;
      state.error = action.payload as string;
    });
    // Accepted
    builder.addCase(getAcceptedOrders.fulfilled, (state, action) => {
      state.acceptedOrders = action.payload;
    });
    // Completed
    builder.addCase(getCompletedOrders.fulfilled, (state, action) => {
      state.completedOrders = action.payload;
    });
    // Respond
    builder.addCase(respondToAssignment.fulfilled, (state, action) => {
      const { orderId, accept } = action.payload;
      state.pendingOrders = state.pendingOrders.filter(o => o.id !== orderId);
      if (accept) {
        // Refetch accepted orders after accepting
      }
    });
    // Update status
    builder.addCase(updateOrderStatus.fulfilled, (state, action) => {
      const { orderId, status } = action.payload;
      if (status === 'DELIVERED') {
        state.acceptedOrders = state.acceptedOrders.filter(o => o.id !== orderId);
      }
    });
  },
});

export default ordersSlice.reducer;
