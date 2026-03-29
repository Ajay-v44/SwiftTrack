"use client"

import { useEffect, useMemo, useState } from "react"
import {
  createDashboardDateRange,
  fetchTenantDashboardOverviewService,
  fetchTenantDeliveryAnalyticsService,
} from "@swifttrack/services"
import {
  TenantDashboardData,
  TenantDashboardDateRange,
  TenantDeliveryAnalytics,
} from "@swifttrack/types"

const initialOverview: TenantDashboardData = {
  walletBalance: 0,
  todayExpenses: 0,
  summary: {
    totalDeliveredOrders: 0,
    activeOrders: 0,
    deliveryVolume: [],
    latestOrders: [],
  },
}

const initialAnalytics: TenantDeliveryAnalytics = {
  ...createDashboardDateRange(30),
  deliveredOrders: 0,
  averagePerDay: 0,
  peakDeliveredOrders: 0,
  peakDate: createDashboardDateRange(30).startDate,
  deliveryVolume: [],
}

const presetDays = {
  "7D": 7,
  "30D": 30,
  "90D": 90,
} as const

export function useTenantDashboard(userId?: string) {
  const [overview, setOverview] = useState<TenantDashboardData>(initialOverview)
  const [analytics, setAnalytics] = useState<TenantDeliveryAnalytics>(initialAnalytics)
  const [selectedRange, setSelectedRange] = useState<TenantDashboardDateRange>(createDashboardDateRange(30))
  const [activePreset, setActivePreset] = useState<keyof typeof presetDays | "CUSTOM">("30D")
  const [overviewLoading, setOverviewLoading] = useState(true)
  const [analyticsLoading, setAnalyticsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let active = true

    async function loadOverview() {
      if (!userId) {
        setOverviewLoading(false)
        return
      }

      setOverviewLoading(true)
      setError(null)

      try {
        const response = await fetchTenantDashboardOverviewService(userId)
        if (active) {
          setOverview(response)
        }
      } catch (err) {
        console.error("Tenant dashboard overview fetch failed", err)
        if (active) {
          setError("Failed to load dashboard data")
        }
      } finally {
        if (active) {
          setOverviewLoading(false)
        }
      }
    }

    loadOverview()

    return () => {
      active = false
    }
  }, [userId])

  useEffect(() => {
    let active = true

    async function loadAnalytics() {
      if (!userId) {
        setAnalyticsLoading(false)
        return
      }

      setAnalyticsLoading(true)

      try {
        const response = await fetchTenantDeliveryAnalyticsService(selectedRange)
        if (active) {
          setAnalytics(response)
        }
      } catch (err) {
        console.error("Tenant delivery analytics fetch failed", err)
        if (active) {
          setError("Failed to load dashboard analytics")
        }
      } finally {
        if (active) {
          setAnalyticsLoading(false)
        }
      }
    }

    loadAnalytics()

    return () => {
      active = false
    }
  }, [selectedRange.endDate, selectedRange.startDate, userId])

  const kpi = useMemo(() => {
    const delivered = analytics.deliveredOrders
    const active = overview.summary.activeOrders
    const throughput = analytics.averagePerDay
    const completionRatio = delivered + active > 0 ? Math.round((delivered / (delivered + active)) * 100) : 0

    return {
      completionRatio,
      throughput,
      peakDeliveredOrders: analytics.peakDeliveredOrders,
      peakDate: analytics.peakDate,
    }
  }, [analytics, overview.summary.activeOrders])

  function applyPresetRange(preset: keyof typeof presetDays) {
    setActivePreset(preset)
    setSelectedRange(createDashboardDateRange(presetDays[preset]))
  }

  function applyCustomRange(range: TenantDashboardDateRange) {
    setActivePreset("CUSTOM")
    setSelectedRange(range)
  }

  return {
    overview,
    analytics,
    selectedRange,
    activePreset,
    overviewLoading,
    analyticsLoading,
    error,
    kpi,
    applyPresetRange,
    applyCustomRange,
  }
}
