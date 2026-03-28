import { httpClient } from "./http-client"
import { serviceEndpoints } from "./endpoints"
import {
  AccountResponse,
  PaginatedLedgerTransactionsResponse,
  TenantDashboardDateRange,
  TenantDashboardSummary,
  TenantDeliveryAnalytics,
  TenantFinanceSummary,
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
