"use client"

import type { ComponentType } from "react"
import { useEffect, useMemo, useState } from "react"
import Link from "next/link"
import {
  AlertCircle,
  CalendarDays,
  CheckCircle2,
  Clock3,
  CreditCard,
  MapPin,
  Package2,
  Rocket,
  Wallet,
} from "lucide-react"
import { useAuthStore } from "@/store/useAuthStore"
import { useTenantDashboard } from "@/hooks/useTenantDashboard"
import { useTenantNotifications } from "@/components/tenant/TenantNotificationsProvider"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"

const currencyFormatter = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
})

const numberFormatter = new Intl.NumberFormat("en-IN")

const statusMeta = {
  DELIVERED: { label: "Delivered", tone: "text-emerald-700 bg-emerald-50 border-emerald-200", icon: CheckCircle2 },
  IN_TRANSIT: { label: "In Transit", tone: "text-sky-700 bg-sky-50 border-sky-200", icon: Rocket },
  PICKED_UP: { label: "Picked Up", tone: "text-sky-700 bg-sky-50 border-sky-200", icon: Rocket },
  ASSIGNED: { label: "Assigned", tone: "text-indigo-700 bg-indigo-50 border-indigo-200", icon: Clock3 },
  CREATED: { label: "Created", tone: "text-violet-700 bg-violet-50 border-violet-200", icon: Package2 },
  CANCELLED: { label: "Cancelled", tone: "text-rose-700 bg-rose-50 border-rose-200", icon: AlertCircle },
  FAILED: { label: "Failed", tone: "text-rose-700 bg-rose-50 border-rose-200", icon: AlertCircle },
} as const

export function TenantDashboardScreen() {
  const { user } = useAuthStore()
  const {
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
  } = useTenantDashboard(user?.id)
  const { unreadCount } = useTenantNotifications()

  const [draftStartDate, setDraftStartDate] = useState(selectedRange.startDate)
  const [draftEndDate, setDraftEndDate] = useState(selectedRange.endDate)

  useEffect(() => {
    setDraftStartDate(selectedRange.startDate)
    setDraftEndDate(selectedRange.endDate)
  }, [selectedRange.endDate, selectedRange.startDate])

  const maxDeliveredCount = useMemo(
    () => Math.max(...analytics.deliveryVolume.map((item) => item.deliveredCount), 1),
    [analytics.deliveryVolume]
  )

  const topCards = [
    {
      title: "Wallet Balance",
      value: currencyFormatter.format(overview.walletBalance),
      helper: "Available operating funds",
      accent: "bg-indigo-50 text-indigo-700",
      icon: Wallet,
      loading: overviewLoading,
    },
    {
      title: "Today's Expenses",
      value: currencyFormatter.format(overview.todayExpenses),
      helper: "Live debit activity",
      accent: "bg-rose-50 text-rose-700",
      icon: CreditCard,
      loading: overviewLoading,
    },
    {
      title: "Delivered Orders",
      value: numberFormatter.format(overview.summary.totalDeliveredOrders),
      helper: "All completed deliveries",
      accent: "bg-emerald-50 text-emerald-700",
      icon: CheckCircle2,
      loading: overviewLoading,
    },
    {
      title: "Active Deliveries",
      value: numberFormatter.format(overview.summary.activeOrders),
      helper: "Orders currently moving",
      accent: "bg-sky-50 text-sky-700",
      icon: Rocket,
      loading: overviewLoading,
    },
  ]

  return (
    <div className="min-h-full bg-slate-50">
      <div className="mx-auto flex w-full max-w-7xl flex-col gap-6 px-6 py-8 lg:px-8">
        <section className="rounded-[28px] border border-slate-200 bg-[radial-gradient(circle_at_top_left,_rgba(99,102,241,0.14),_transparent_30%),linear-gradient(135deg,#ffffff,#f8fafc)] p-8 shadow-sm">
          <div className="flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
            <div className="max-w-2xl space-y-3">
              <Badge variant="outline" className="rounded-full border-indigo-200 bg-white/80 px-3 py-1 text-indigo-700">
                Frontline tenant analytics
              </Badge>
              <div className="space-y-2">
                <h1 className="text-3xl font-semibold tracking-tight text-slate-950">
                  Delivery visibility for {user?.name?.split(" ")[0] || "your team"}
                </h1>
                <p className="max-w-xl text-sm leading-6 text-slate-600">
                  Monitor delivery throughput, operational risk, wallet health, and live activity from one surface.
                </p>
              </div>
            </div>

            <div className="flex flex-wrap gap-3">
              <Button asChild className="rounded-full bg-slate-950 text-white hover:bg-slate-800">
                <Link href="/tenant/orders/create">Create Order</Link>
              </Button>
              <Button asChild variant="outline" className="rounded-full border-slate-300 bg-white">
                <Link href="/tenant/finance">Top Up Wallet</Link>
              </Button>
              <Button asChild variant="outline" className="rounded-full border-slate-300 bg-white">
                <Link href="/tenant/orders">View Orders</Link>
              </Button>
            </div>
          </div>

          {error ? (
            <div className="mt-6 rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
              {error}
            </div>
          ) : null}
        </section>

        <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          {topCards.map((card) => (
            <MetricCard key={card.title} {...card} />
          ))}
        </section>

        <section className="grid gap-6 xl:grid-cols-[minmax(0,1.6fr)_minmax(360px,0.9fr)]">
          <Card className="overflow-hidden border-slate-200 bg-white shadow-sm">
            <CardHeader className="gap-4 border-b border-slate-100 pb-6">
              <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                <div className="space-y-1">
                  <CardTitle className="text-xl text-slate-950">Delivery Volume</CardTitle>
                  <CardDescription>
                    Daily delivered order counts for the selected date range.
                  </CardDescription>
                </div>

                <div className="flex flex-col gap-3 lg:items-end">
                  <div className="flex flex-wrap gap-2">
                    {(["7D", "30D", "90D"] as const).map((preset) => (
                      <button
                        key={preset}
                        type="button"
                        onClick={() => applyPresetRange(preset)}
                        className={`rounded-full border px-3 py-1.5 text-xs font-medium transition ${
                          activePreset === preset
                            ? "border-slate-950 bg-slate-950 text-white"
                            : "border-slate-200 bg-white text-slate-600 hover:border-slate-300"
                        }`}
                      >
                        {preset}
                      </button>
                    ))}
                  </div>

                  <div className="flex flex-wrap items-end gap-2">
                    <div className="space-y-1">
                      <label className="text-[11px] font-medium uppercase tracking-[0.18em] text-slate-500">
                        Start
                      </label>
                      <Input
                        type="date"
                        value={draftStartDate}
                        onChange={(event) => setDraftStartDate(event.target.value)}
                        className="h-9 w-[152px] rounded-full border-slate-200 bg-white"
                      />
                    </div>
                    <div className="space-y-1">
                      <label className="text-[11px] font-medium uppercase tracking-[0.18em] text-slate-500">
                        End
                      </label>
                      <Input
                        type="date"
                        value={draftEndDate}
                        onChange={(event) => setDraftEndDate(event.target.value)}
                        className="h-9 w-[152px] rounded-full border-slate-200 bg-white"
                      />
                    </div>
                    <Button
                      type="button"
                      variant="outline"
                      className="rounded-full border-slate-200"
                      onClick={() =>
                        applyCustomRange({
                          startDate: draftStartDate,
                          endDate: draftEndDate,
                        })
                      }
                      disabled={!draftStartDate || !draftEndDate}
                    >
                      Apply
                    </Button>
                  </div>
                </div>
              </div>
            </CardHeader>

            <CardContent className="space-y-6 pt-6">
              <div className="grid gap-3 md:grid-cols-3">
                <AnalyticsBadge
                  label="Range Orders"
                  value={numberFormatter.format(analytics.deliveredOrders)}
                  loading={analyticsLoading}
                />
                <AnalyticsBadge
                  label="Avg / Day"
                  value={analytics.averagePerDay.toFixed(1)}
                  loading={analyticsLoading}
                />
                <AnalyticsBadge
                  label="Peak Day"
                  value={
                    analytics.peakDeliveredOrders > 0
                      ? `${analytics.peakDeliveredOrders} on ${formatShortDate(analytics.peakDate)}`
                      : "No peak yet"
                  }
                  loading={analyticsLoading}
                />
              </div>

              <div className="rounded-3xl border border-slate-200 bg-slate-50 p-4">
                <div className="mb-4 flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-slate-900">Delivered orders per day</p>
                    <p className="text-xs text-slate-500">
                      {formatShortDate(analytics.startDate)} to {formatShortDate(analytics.endDate)}
                    </p>
                  </div>
                  <div className="flex items-center gap-2 text-xs text-slate-500">
                    <CalendarDays className="h-4 w-4" />
                    Auto-filled with zeroes for quiet days
                  </div>
                </div>

                <div className="grid h-72 grid-cols-[repeat(auto-fit,minmax(10px,1fr))] items-end gap-2">
                  {analyticsLoading
                    ? Array.from({ length: 24 }, (_, index) => (
                        <div key={index} className="flex h-full flex-col justify-end gap-2">
                          <div
                            className="rounded-t-full bg-slate-200 animate-pulse"
                            style={{ height: `${25 + (index % 6) * 10}%` }}
                          />
                          <div className="h-2 rounded-full bg-slate-200 animate-pulse" />
                        </div>
                      ))
                    : analytics.deliveryVolume.map((point, index) => (
                        <div key={point.date} className="group flex h-full flex-col justify-end gap-2">
                          <div className="relative flex-1 rounded-full bg-white px-1 pt-2 shadow-inner">
                            <div
                              className="absolute inset-x-1 bottom-1 rounded-full bg-gradient-to-t from-indigo-600 via-sky-500 to-cyan-300 transition-all duration-500"
                              style={{
                                height: `${Math.max((point.deliveredCount / maxDeliveredCount) * 100, point.deliveredCount > 0 ? 10 : 3)}%`,
                              }}
                              title={`${point.deliveredCount} deliveries on ${point.date}`}
                            />
                          </div>
                          <span className="text-center text-[10px] text-slate-500">
                            {shouldRenderAxisLabel(index, analytics.deliveryVolume.length)
                              ? new Date(point.date).toLocaleDateString("en-US", { month: "short", day: "numeric" })
                              : ""}
                          </span>
                        </div>
                      ))}
                </div>
              </div>

              <div className="grid gap-3 md:grid-cols-3">
                <InsightCard
                  title="Completion Ratio"
                  value={`${kpi.completionRatio}%`}
                  description="Delivered vs active workload in the current dashboard view."
                />
                <InsightCard
                  title="Throughput"
                  value={`${kpi.throughput.toFixed(1)}/day`}
                  description="Average delivered orders per day in the selected range."
                />
                <InsightCard
                  title="Best Day"
                  value={
                    kpi.peakDeliveredOrders > 0
                      ? `${kpi.peakDeliveredOrders} orders`
                      : "No delivered orders"
                  }
                  description={
                    kpi.peakDeliveredOrders > 0
                      ? `Peak volume landed on ${formatLongDate(kpi.peakDate)}.`
                      : "Expand the date range or wait for delivery activity."
                  }
                />
              </div>
            </CardContent>
          </Card>

          <Card className="border-slate-200 bg-white shadow-sm">
            <CardHeader className="border-b border-slate-100 pb-4">
              <div className="flex items-center justify-between gap-3">
                <div>
                  <CardTitle className="text-xl text-slate-950">Operations Feed</CardTitle>
                  <CardDescription>Live status plus a notification surface ready for API wiring.</CardDescription>
                </div>
                <Badge variant="outline" className="rounded-full border-slate-200 bg-slate-50 text-slate-700">
                  Recent orders
                </Badge>
              </div>
            </CardHeader>

            <CardContent className="pt-6">
              <div className="mb-4 flex items-center gap-2 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-600">
                <Rocket className="h-4 w-4 shrink-0 text-sky-600" />
                <span>Live order updates stay here. All notifications now open from the top bell icon.</span>
              </div>

              <div className="space-y-3">
                {overviewLoading ? (
                  Array.from({ length: 3 }, (_, index) => <FeedSkeleton key={index} />)
                ) : overview.summary.latestOrders.length > 0 ? (
                  overview.summary.latestOrders.map((order) => {
                    const meta = statusMeta[order.orderStatus as keyof typeof statusMeta] || {
                      label: order.orderStatus,
                      tone: "text-slate-700 bg-slate-50 border-slate-200",
                      icon: Clock3,
                    }
                    const StatusIcon = meta.icon

                    return (
                      <Link
                        key={order.id}
                        href={`/tenant/orders?orderId=${order.id}`}
                        className="block rounded-2xl border border-slate-200 bg-slate-50 p-4 transition hover:border-slate-300 hover:bg-white"
                      >
                        <div className="flex items-start justify-between gap-3">
                          <div className="flex min-w-0 items-start gap-3">
                            <div className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-full border ${meta.tone}`}>
                              <StatusIcon className="h-4 w-4 shrink-0" />
                            </div>
                            <div className="min-w-0 space-y-1">
                              <p className="truncate text-sm font-medium text-slate-950">
                                {order.customerReferenceId || order.id.slice(0, 8)}
                              </p>
                              <p className="flex items-center gap-1 text-xs text-slate-500">
                                <MapPin className="h-3.5 w-3.5 shrink-0" />
                                <span className="truncate">{order.city || "Unknown city"}</span>
                              </p>
                              <p className="text-xs text-slate-500">{formatRelativeTime(order.createdAt)}</p>
                            </div>
                          </div>
                          <Badge variant="outline" className={`shrink-0 rounded-full ${meta.tone}`}>
                            {meta.label}
                          </Badge>
                        </div>
                      </Link>
                    )
                  })
                ) : (
                  <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
                    No recent tenant orders found.
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        </section>

        <section className="grid gap-6 lg:grid-cols-[minmax(0,1.2fr)_minmax(320px,0.8fr)]">
          <Card className="border-slate-200 bg-white shadow-sm">
            <CardHeader className="border-b border-slate-100 pb-5">
              <CardTitle className="text-xl text-slate-950">Operational Snapshot</CardTitle>
              <CardDescription>Quick metrics to help decide whether to dispatch, top up, or rebalance.</CardDescription>
            </CardHeader>
            <CardContent className="grid gap-4 pt-6 md:grid-cols-3">
              <SnapshotPanel
                title="Range Window"
                value={`${Math.round((new Date(`${selectedRange.endDate}T00:00:00`).getTime() - new Date(`${selectedRange.startDate}T00:00:00`).getTime()) / 86_400_000) + 1} days`}
                description="Currently applied analytics period."
              />
              <SnapshotPanel
                title="Unread Updates"
                value={`${unreadCount}`}
                description="Open the header bell to review or delete notifications."
              />
              <SnapshotPanel
                title="Latest Orders"
                value={`${overview.summary.latestOrders.length}`}
                description="Recent orders shown in the live status tab."
              />
            </CardContent>
          </Card>

          <Card className="border-slate-200 bg-slate-950 text-white shadow-sm">
            <CardHeader className="border-b border-white/10 pb-5">
              <CardTitle className="text-xl">Action Queue</CardTitle>
              <CardDescription className="text-slate-300">
                Prioritized next steps based on the current dashboard state.
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-3 pt-6">
              <ActionRow
                title="Review chart range"
                description="Use a shorter range to isolate recent delivery volatility or a longer one for trend analysis."
              />
              <ActionRow
                title="Watch wallet vs expenses"
                description="Finance and delivery activity are now on one screen, so you can top up before dispatch slows."
              />
              <ActionRow
                title="Track fresh orders"
                description="The live status tab surfaces recent tenant orders without leaving the dashboard."
              />
            </CardContent>
          </Card>
        </section>
      </div>
    </div>
  )
}

function MetricCard({
  title,
  value,
  helper,
  accent,
  icon: Icon,
  loading,
}: {
  title: string
  value: string
  helper: string
  accent: string
  icon: ComponentType<{ className?: string }>
  loading: boolean
}) {
  return (
    <Card className="border-slate-200 bg-white shadow-sm">
      <CardContent className="flex items-start justify-between gap-4 pt-6">
        <div className="space-y-2">
          <p className="text-xs font-medium uppercase tracking-[0.2em] text-slate-500">{title}</p>
          {loading ? <DashboardSkeleton className="h-9 w-32" /> : <p className="text-3xl font-semibold text-slate-950">{value}</p>}
          <p className="text-sm text-slate-500">{helper}</p>
        </div>
        <div className={`rounded-2xl p-3 ${accent}`}>
          <Icon className="h-5 w-5" />
        </div>
      </CardContent>
    </Card>
  )
}

function AnalyticsBadge({
  label,
  value,
  loading,
}: {
  label: string
  value: string
  loading: boolean
}) {
  return (
    <div className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3">
      <p className="text-xs font-medium uppercase tracking-[0.18em] text-slate-500">{label}</p>
      {loading ? <DashboardSkeleton className="mt-2 h-7 w-24" /> : <p className="mt-2 text-lg font-semibold text-slate-950">{value}</p>}
    </div>
  )
}

function InsightCard({
  title,
  value,
  description,
}: {
  title: string
  value: string
  description: string
}) {
  return (
    <div className="rounded-2xl border border-slate-200 bg-white px-4 py-4">
      <p className="text-sm font-medium text-slate-950">{title}</p>
      <p className="mt-2 text-2xl font-semibold text-slate-950">{value}</p>
      <p className="mt-2 text-sm leading-6 text-slate-500">{description}</p>
    </div>
  )
}

function SnapshotPanel({
  title,
  value,
  description,
}: {
  title: string
  value: string
  description: string
}) {
  return (
    <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
      <p className="text-xs font-medium uppercase tracking-[0.18em] text-slate-500">{title}</p>
      <p className="mt-2 text-2xl font-semibold text-slate-950">{value}</p>
      <p className="mt-2 text-sm leading-6 text-slate-500">{description}</p>
    </div>
  )
}

function ActionRow({ title, description }: { title: string; description: string }) {
  return (
    <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
      <p className="text-sm font-medium">{title}</p>
      <p className="mt-1 text-sm leading-6 text-slate-300">{description}</p>
    </div>
  )
}

function FeedSkeleton() {
  return (
    <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
      <DashboardSkeleton className="h-4 w-24" />
      <DashboardSkeleton className="mt-3 h-4 w-40" />
      <DashboardSkeleton className="mt-2 h-3 w-full" />
      <DashboardSkeleton className="mt-2 h-3 w-4/5" />
    </div>
  )
}

function DashboardSkeleton({ className }: { className?: string }) {
  return <div className={`animate-pulse rounded-xl bg-slate-200 ${className || ""}`.trim()} />
}

function shouldRenderAxisLabel(index: number, length: number) {
  if (length <= 7) {
    return true
  }

  const labelSpacing = Math.max(Math.floor(length / 5), 1)
  return index === 0 || index === length - 1 || index % labelSpacing === 0
}

function formatShortDate(date: string) {
  return new Date(date).toLocaleDateString("en-US", { month: "short", day: "numeric" })
}

function formatLongDate(date: string) {
  return new Date(date).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" })
}

function formatRelativeTime(date: string) {
  const diff = Date.now() - new Date(date).getTime()
  const minutes = Math.max(Math.round(diff / 60_000), 0)

  if (minutes < 1) {
    return "just now"
  }

  if (minutes < 60) {
    return `${minutes}m ago`
  }

  const hours = Math.round(minutes / 60)
  if (hours < 24) {
    return `${hours}h ago`
  }

  const days = Math.round(hours / 24)
  return `${days}d ago`
}
