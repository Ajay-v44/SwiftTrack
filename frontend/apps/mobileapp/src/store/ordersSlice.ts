import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import apiClient from '../api/client';

export interface Order {
  id: string;
  orderNumber: string;
  status: string;
  pickupLocation: { address: string; lat: number; lng: number };
  dropoffLocation: { address: string; lat: number; lng: number };
  customerDetails: { name: string; phone: string };
  paymentStatus: string;
  createdAt: string;
  eta: string;
  assignmentId?: string;
}

interface OrdersState {
  orders: Order[];
  loading: boolean;
  error: string | null;
}

const initialState: OrdersState = {
  orders: [],
  loading: false,
  error: null,
};

export const getMyOrders = createAsyncThunk('orders/getMyOrders', async (_, { rejectWithValue }) => {
  try {
    const response = await apiClient.get('/driverservice/api/driver/v1/getMyOrders');
    return response.data;
  } catch (error: any) {
    return rejectWithValue(error.response?.data?.message || 'Failed to fetch orders');
  }
});

export const respondToAssignment = createAsyncThunk(
  'orders/respondAssignment',
  async ({ assignmentId, response }: { assignmentId: string; response: 'ACCEPT' | 'REJECT' }, { rejectWithValue }) => {
    try {
      await apiClient.post('/driverservice/api/driver/v1/respond-assignment', { assignmentId, response });
      return { assignmentId, response };
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to respond to assignment');
    }
  }
);

export const updateOrderStatus = createAsyncThunk(
  'orders/updateStatus',
  async ({ orderId, status }: { orderId: string; status: string }, { rejectWithValue }) => {
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
    builder.addCase(getMyOrders.pending, (state) => {
      state.loading = true;
      state.error = null;
    });
    builder.addCase(getMyOrders.fulfilled, (state, action) => {
      state.loading = false;
      state.orders = action.payload;
    });
    builder.addCase(getMyOrders.rejected, (state, action) => {
      state.loading = false;
      state.error = action.payload as string;
    });

    builder.addCase(respondToAssignment.fulfilled, (state, action) => {
      if (action.payload.response === 'REJECT') {
        state.orders = state.orders.filter(o => o.assignmentId !== action.payload.assignmentId);
      } else {
        const order = state.orders.find(o => o.assignmentId === action.payload.assignmentId);
        if (order) {
          order.status = 'ACCEPTED';
        }
      }
    });

    builder.addCase(updateOrderStatus.fulfilled, (state, action) => {
      const order = state.orders.find(o => o.id === action.payload.orderId);
      if (order) {
        order.status = action.payload.status;
      }
    });
  },
});

export default ordersSlice.reducer;
