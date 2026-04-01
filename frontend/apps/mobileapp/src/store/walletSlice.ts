import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import apiClient from '../api/client';

export const fetchWalletDetails = createAsyncThunk('wallet/fetchDetails', async (_, { rejectWithValue }) => {
  try {
    const response = await apiClient.get('/BillingService/api/billing/v1/wallet');
    return response.data;
  } catch (error: any) {
    return rejectWithValue(error.response?.data?.message || 'Failed to fetch wallet details');
  }
});

export const fetchTransactions = createAsyncThunk('wallet/fetchTransactions', async (_, { rejectWithValue }) => {
  try {
    const response = await apiClient.get('/BillingService/api/billing/v1/wallet/transactions');
    return response.data;
  } catch (error: any) {
    return rejectWithValue(error.response?.data?.message || 'Failed to fetch transactions');
  }
});

const walletSlice = createSlice({
  name: 'wallet',
  initialState: {
    balance: 0,
    transactions: [],
    loading: false,
    error: null,
  },
  reducers: {},
  extraReducers: (builder) => {
    builder.addCase(fetchWalletDetails.fulfilled, (state, action) => {
      state.balance = action.payload.balance || 0;
    });
    builder.addCase(fetchTransactions.pending, (state) => {
      state.loading = true;
    });
    builder.addCase(fetchTransactions.fulfilled, (state, action) => {
      state.loading = false;
      state.transactions = action.payload || [];
    });
    builder.addCase(fetchTransactions.rejected, (state, action: any) => {
      state.loading = false;
      state.error = action.payload;
    });
  },
});

export default walletSlice.reducer;
