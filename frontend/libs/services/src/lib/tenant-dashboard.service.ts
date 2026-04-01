import {
  fetchTenantAccountApi,
  fetchTenantDashboardSummaryApi,
  fetchTenantDeliveryAnalyticsApi,
  fetchTenantTodayExpensesApi,
} from "@swifttrack/api-client"
import {
  TenantDashboardData,
  TenantDashboardDateRange,
  TenantDashboardNotification,
  TenantDashboardSummary,
  TenantDeliveryAnalytics,
} from "@swifttrack/types"

const DEFAULT_WINDOW_DAYS = 30

const emptySummary: TenantDashboardSummary = {
  totalDeliveredOrders: 0,
  activeOrders: 0,
  deliveryVolume: [],
  latestOrders: [],
}

export function createDashboardDateRange(days: number): TenantDashboardDateRange {
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(endDate.getDate() - (days - 1))

  return {
    startDate: startDate.toISOString().slice(0, 10),
    endDate: endDate.toISOString().slice(0, 10),
  }
}

export async function fetchTenantDashboardOverviewService(userId: string): Promise<TenantDashboardData> {
  const [accountResponse, expenseResponse, summaryResponse] = await Promise.all([
    fetchTenantAccountApi(userId),
    fetchTenantTodayExpensesApi(),
    fetchTenantDashboardSummaryApi(),
  ])

  return {
    walletBalance: accountResponse.data?.balance || 0,
    todayExpenses: expenseResponse.data?.amount || 0,
    summary: normalizeSummary(summaryResponse.data),
  }
}

export async function fetchTenantDeliveryAnalyticsService(
  range: TenantDashboardDateRange = createDashboardDateRange(DEFAULT_WINDOW_DAYS)
): Promise<TenantDeliveryAnalytics> {
  const response = await fetchTenantDeliveryAnalyticsApi(range)
  return normalizeAnalytics(response.data, range)
}

export async function fetchTenantNotificationsService(): Promise<TenantDashboardNotification[]> {
  return []
}

function normalizeSummary(summary?: TenantDashboardSummary): TenantDashboardSummary {
  return {
    ...(summary || emptySummary),
    deliveryVolume: summary?.deliveryVolume || [],
    latestOrders: summary?.latestOrders || [],
  }
}

function normalizeAnalytics(
  analytics: TenantDeliveryAnalytics | undefined,
  fallbackRange: TenantDashboardDateRange
): TenantDeliveryAnalytics {
  if (analytics?.deliveryVolume?.length) {
    return analytics
  }

  const deliveryVolume = buildEmptyVolumeSeries(fallbackRange)

  return {
    startDate: analytics?.startDate || fallbackRange.startDate,
    endDate: analytics?.endDate || fallbackRange.endDate,
    deliveredOrders: analytics?.deliveredOrders || 0,
    averagePerDay: analytics?.averagePerDay || 0,
    peakDeliveredOrders: analytics?.peakDeliveredOrders || 0,
    peakDate: analytics?.peakDate || fallbackRange.startDate,
    deliveryVolume,
  }
}

function buildEmptyVolumeSeries(range: TenantDashboardDateRange) {
  const start = new Date(`${range.startDate}T00:00:00`)
  const end = new Date(`${range.endDate}T00:00:00`)
  const points = []

  for (const cursor = new Date(start); cursor <= end; cursor.setDate(cursor.getDate() + 1)) {
    points.push({
      date: cursor.toISOString().slice(0, 10),
      deliveredCount: 0,
    })
  }

  return points
}
