import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import apiClient from '../api/client';

/**
 * Backend AccountController (BillingAndSettlementService):
 * - GET /api/accounts/v1/getMyAccount  → Account { id, userId, accountType, balance, currency, isActive, ... }
 * - GET /api/accounts/v1/getTransactions?page=0&size=20  → PaginatedLedgerTransactionsResponse
 * Service name in Eureka: BillingAndSettlementService → gateway: /billingandsettlementservice/
 */

export const fetchWalletDetails = createAsyncThunk('wallet/fetchDetails', async (_, { rejectWithValue }) => {
  try {
    const response = await apiClient.get('/billingandsettlementservice/api/accounts/v1/getMyAccount');
    return response.data;
  } catch (error: any) {
    return rejectWithValue(error.response?.data?.message || 'Failed to fetch wallet details');
  }
});

export const fetchTransactions = createAsyncThunk('wallet/fetchTransactions', async (_, { rejectWithValue }) => {
  try {
    const response = await apiClient.get('/billingandsettlementservice/api/accounts/v1/getTransactions', {
      params: { page: 0, size: 20 },
    });
    return response.data;
  } catch (error: any) {
    return rejectWithValue(error.response?.data?.message || 'Failed to fetch transactions');
  }
});

interface WalletState {
  balance: number;
  currency: string;
  accountId: string | null;
  transactions: any[];
  totalPages: number;
  loading: boolean;
  error: string | null;
}

const initialState: WalletState = {
  balance: 0,
  currency: 'INR',
  accountId: null,
  transactions: [],
  totalPages: 0,
  loading: false,
  error: null,
};

const walletSlice = createSlice({
  name: 'wallet',
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder.addCase(fetchWalletDetails.fulfilled, (state, action) => {
      // Account fields: id, userId, accountType, balance, currency, isActive
      state.balance = parseFloat(action.payload?.balance) || 0;
      state.currency = action.payload?.currency || 'INR';
      state.accountId = action.payload?.id || null;
    });
    builder.addCase(fetchTransactions.pending, (state) => {
      state.loading = true;
    });
    builder.addCase(fetchTransactions.fulfilled, (state, action) => {
      state.loading = false;
      // PaginatedLedgerTransactionsResponse: { items: LedgerTransactionListItemResponse[], page, size, totalElements, totalPages }
      const data = action.payload;
      if (data?.items) {
        state.transactions = data.items;
        state.totalPages = data.totalPages || 0;
      } else if (Array.isArray(data)) {
        state.transactions = data;
      } else {
        state.transactions = [];
      }
    });
    builder.addCase(fetchTransactions.rejected, (state, action: any) => {
      state.loading = false;
      state.error = action.payload;
    });
  },
});

export default walletSlice.reducer;
