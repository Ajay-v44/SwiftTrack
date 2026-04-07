import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import apiClient from '../api/client';
import { logout } from './authSlice';

/**
 * Backend AccountController (BillingAndSettlementService):
 * - GET /api/accounts/v1/getMyAccount  → Account { id, userId, accountType, balance, currency, isActive, ... }
 * - GET /api/accounts/v1/getTransactions?page=0&size=20  → PaginatedLedgerTransactionsResponse
 * Service name in Eureka: BillingAndSettlementService → gateway: /billingandsettlementservice/
 */

const getDriverUserId = (state: any) => state.auth?.driver?.user?.id as string | undefined;
const getDriverUserType = (state: any) => state.auth?.driver?.user?.userType as string | undefined;

async function fetchMyAccount(accountType: 'DRIVER' | 'TENANT_DRIVER') {
  return apiClient.get('/billingandsettlementservice/api/accounts/v1/getMyAccount', {
    params: { accountType },
  });
}

export const fetchWalletDetails = createAsyncThunk(
  'wallet/fetchDetails',
  async (_, { getState, rejectWithValue }) => {
    const userId = getDriverUserId(getState());
    const userType = getDriverUserType(getState());
    if (!userId) {
      return rejectWithValue('User details not loaded yet');
    }

    try {
      const response = await fetchMyAccount(userType === 'TENANT_DRIVER' ? 'TENANT_DRIVER' : 'DRIVER');
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch wallet details');
    }
  }
);

export const fetchTransactions = createAsyncThunk(
  'wallet/fetchTransactions',
  async (_, { getState, rejectWithValue }) => {
    const state = getState() as any;
    const userId = getDriverUserId(state);
    const userType = getDriverUserType(state);
    if (!userId) {
      return rejectWithValue('User details not loaded yet');
    }

    try {
      let accountId = state.wallet?.accountId as string | null;

      if (!accountId) {
        const accountResponse = await fetchMyAccount(userType === 'TENANT_DRIVER' ? 'TENANT_DRIVER' : 'DRIVER');
        accountId = accountResponse.data?.id || null;
      }

      if (!accountId) {
        return rejectWithValue('Account not found');
      }

      const response = await apiClient.get('/billingandsettlementservice/api/accounts/v1/getTransactions', {
        params: { accountId, page: 0, size: 20 },
      });
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch transactions');
    }
  }
);

export interface WalletState {
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
    builder.addCase(fetchWalletDetails.pending, (state) => {
      state.loading = true;
      state.error = null;
    });
    builder.addCase(fetchWalletDetails.fulfilled, (state, action) => {
      state.loading = false;
      // Account fields: id, userId, accountType, balance, currency, isActive
      state.balance = parseFloat(action.payload?.balance) || 0;
      state.currency = action.payload?.currency || 'INR';
      state.accountId = action.payload?.id || null;
    });
    builder.addCase(fetchWalletDetails.rejected, (state, action: any) => {
      state.loading = false;
      state.error = action.payload;
    });
    builder.addCase(fetchTransactions.pending, (state) => {
      state.loading = true;
      state.error = null;
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
    builder.addCase(logout.fulfilled, () => initialState);
  },
});

export default walletSlice.reducer;
