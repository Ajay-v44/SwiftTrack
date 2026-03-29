export interface TenantDashboardVolumePoint {
  date: string
  deliveredCount: number
}

export interface TenantDeliveryAnalytics {
  startDate: string
  endDate: string
  deliveredOrders: number
  averagePerDay: number
  peakDeliveredOrders: number
  peakDate: string
  deliveryVolume: TenantDashboardVolumePoint[]
}

export interface TenantDashboardDateRange {
  startDate: string
  endDate: string
}

export interface TenantDashboardOrder {
  id: string
  customerReferenceId: string | null
  orderStatus: string
  city: string
  createdAt: string
}

export interface TenantDashboardSummary {
  totalDeliveredOrders: number
  activeOrders: number
  deliveryVolume: TenantDashboardVolumePoint[]
  latestOrders: TenantDashboardOrder[]
}

export interface TenantDashboardData {
  walletBalance: number
  todayExpenses: number
  summary: TenantDashboardSummary
}

export interface TenantDashboardNotification {
  id: string
  title: string
  message: string
  severity: "info" | "success" | "warning"
  createdAt: string
  unread: boolean
  actionLabel?: string
}
