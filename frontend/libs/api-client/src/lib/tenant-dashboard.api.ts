import { httpClient } from "./http-client"
import { serviceEndpoints } from "./endpoints"
import {
  AccountResponse,
  PaginatedLedgerTransactionsResponse,
  TenantOrderDetailsResponse,
  TenantOrderTrackingResponse,
  PaginatedTenantOrdersResponse,
  TenantDashboardDateRange,
  TenantDashboardSummary,
  TenantDeliveryAnalytics,
  TenantFinanceSummary,
  TenantOrdersFilterInput,
  TodayExpenseResponse,
} from "@swifttrack/types"

export function fetchTenantAccountApi(userId?: string) {
  return httpClient.get<AccountResponse>(`${serviceEndpoints.billingAccounts}/v1/getMyAccount`, {
    params: userId ? { userId } : undefined,
  })
}

export function fetchTenantTodayExpensesApi() {
  return httpClient.get<TodayExpenseResponse>(
    `${serviceEndpoints.billingAccounts}/v1/dashboard/recent-expenses/today`
  )
}

export function fetchTenantDashboardSummaryApi() {
  return httpClient.get<TenantDashboardSummary>(`${serviceEndpoints.orders}/v1/tenant/dashboard`)
}

export function fetchTenantDeliveryAnalyticsApi(range: TenantDashboardDateRange) {
  return httpClient.get<TenantDeliveryAnalytics>(`${serviceEndpoints.tenantCompany}/v1/dashboard/delivery-volume`, {
    params: range,
  })
}

export function fetchTenantFinanceSummaryApi() {
  return httpClient.get<TenantFinanceSummary>(`${serviceEndpoints.billingAccounts}/v1/dashboard/summary`)
}

export function fetchTenantTransactionsApi(page: number, size: number) {
  return httpClient.get<PaginatedLedgerTransactionsResponse>(
    `${serviceEndpoints.billingAccounts}/v1/getTransactions`,
    {
      params: { page, size },
    }
  )
}

export function fetchTenantOrdersApi(filters: TenantOrdersFilterInput) {
  const { page, size, startDate, endDate } = filters

  return httpClient.get<PaginatedTenantOrdersResponse>(`${serviceEndpoints.orders}/v1/tenant/orders`, {
    params: {
      page,
      size,
      startDate,
      endDate,
    },
  })
}

export function searchTenantOrdersApi(filters: TenantOrdersFilterInput & { query: string }) {
  const { query, page, size, startDate, endDate } = filters

  return httpClient.get<PaginatedTenantOrdersResponse>(`${serviceEndpoints.orders}/v1/tenant/orders/search`, {
    params: {
      query,
      page,
      size,
      startDate,
      endDate,
    },
  })
}

export function fetchTenantOrderDetailsApi(orderId: string) {
  return httpClient.get<TenantOrderDetailsResponse>(`${serviceEndpoints.orders}/v1/getOrderById/${orderId}`)
}

export function fetchTenantOrderTrackingApi(orderId: string) {
  return httpClient.get<TenantOrderTrackingResponse>(`${serviceEndpoints.orders}/v1/getOrderTracking/${orderId}`)
}
