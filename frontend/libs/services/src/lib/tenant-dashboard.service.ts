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
  const now = Date.now()

  return [
    {
      id: "ops-spike",
      title: "Dispatch volume trending up",
      message: "Delivered orders are up in your active date range. Monitor staffing before the next peak.",
      severity: "success",
      createdAt: new Date(now - 8 * 60 * 1000).toISOString(),
      unread: true,
      actionLabel: "Review staffing",
    },
    {
      id: "wallet-watch",
      title: "Wallet threshold watch",
      message: "Today's expenses are posting in real time. Keep wallet balance above your daily operating buffer.",
      severity: "warning",
      createdAt: new Date(now - 21 * 60 * 1000).toISOString(),
      unread: true,
      actionLabel: "Open finance",
    },
    {
      id: "route-focus",
      title: "Latest orders need attention",
      message: "Use the live status panel to inspect newly created or in-transit orders without leaving the dashboard.",
      severity: "info",
      createdAt: new Date(now - 55 * 60 * 1000).toISOString(),
      unread: true,
      actionLabel: "View orders",
    },
  ]
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
