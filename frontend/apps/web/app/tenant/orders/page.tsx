"use client"

import type { ComponentType } from "react"
import { useEffect, useState } from "react"
import Link from "next/link"
import {
  AlertTriangle,
  CalendarDays,
  CircleSlash,
  ChevronLeft,
  ChevronRight,
  Clock3,
  Download,
  Filter,
  Loader2,
  MapPin,
  PackageCheck,
  PackageSearch,
  PlusCircle,
  Search,
  Truck,
} from "lucide-react"
import {
  buildFallbackTenantOrderDetails,
  cancelTenantOrderService,
  fetchTenantOrderDetailsService,
  fetchTenantOrderTrackingService,
} from "@swifttrack/services"
import { toast } from "sonner"
import type { TenantOrderDetailsResponse, TenantOrderTrackingResponse } from "@swifttrack/types"
import { TenantOrderDetailsSheet } from "@/components/tenant/TenantOrderDetailsSheet"
import { useTenantOrders } from "@/hooks/useTenantOrders"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { useTenantSetupGuard } from "@/hooks/useTenantSetupGuard"

const statusTone = {
  CREATED: "bg-slate-100 text-slate-700",
  QUOTED: "bg-violet-50 text-violet-700",
  ASSIGNED: "bg-indigo-50 text-indigo-700",
  PICKED_UP: "bg-sky-50 text-sky-700",
  IN_TRANSIT: "bg-sky-50 text-sky-700",
  DELIVERED: "bg-emerald-50 text-emerald-700",
  CANCELLED: "bg-rose-50 text-rose-700",
  FAILED: "bg-rose-50 text-rose-700",
} as const

export default function TenantOrdersPage() {
  const { createOrderHref } = useTenantSetupGuard()
  const { orders, loading, error, query, setQuery, dateRange, setDateRange, page, setPage, pagination, refresh } =
    useTenantOrders()
  const [selectedOrderId, setSelectedOrderId] = useState<string | null>(null)
  const [detailsOpen, setDetailsOpen] = useState(false)
  const [detailsLoading, setDetailsLoading] = useState(false)
  const [detailsError, setDetailsError] = useState<string | null>(null)
  const [orderDetails, setOrderDetails] = useState<TenantOrderDetailsResponse | null>(null)
  const [orderTracking, setOrderTracking] = useState<TenantOrderTrackingResponse | null>(null)
  const [cancellingOrderId, setCancellingOrderId] = useState<string | null>(null)

  useEffect(() => {
    if (typeof window === "undefined") {
      return
    }

    const orderId = new URLSearchParams(window.location.search).get("orderId")
    if (!orderId) {
      return
    }

    setSelectedOrderId((current) => current ?? orderId)
    setDetailsOpen(true)
  }, [])

  useEffect(() => {
    if (!selectedOrderId || !detailsOpen) {
      return
    }

    const orderId = selectedOrderId
    let active = true

    async function loadOrderDetails() {
      setDetailsLoading(true)
      setDetailsError(null)
      const summaryOrder = orders.items.find((item) => item.id === orderId) ?? null

      try {
        const [details, tracking] = await Promise.all([
          fetchTenantOrderDetailsService(orderId),
          fetchTenantOrderTrackingService(orderId).catch(() => null),
        ])

        if (!active) {
          return
        }

        setOrderDetails(details)
        setOrderTracking(tracking)
      } catch (fetchError) {
        console.error("Order analytics fetch failed", fetchError)
        if (active) {
          setDetailsError(summaryOrder ? "Showing limited details because the detailed order API failed." : "Failed to load order details")
          setOrderDetails(summaryOrder ? buildFallbackTenantOrderDetails(summaryOrder) : null)
          setOrderTracking(null)
        }
      } finally {
        if (active) {
          setDetailsLoading(false)
        }
      }
    }

    void loadOrderDetails()

    return () => {
      active = false
    }
  }, [detailsOpen, orders.items, selectedOrderId])

  function openOrderDetails(orderId: string) {
    setSelectedOrderId(orderId)
    setDetailsOpen(true)
  }

  async function handleCancelOrder(orderId: string) {
    setCancellingOrderId(orderId)
    try {
      await cancelTenantOrderService(orderId)
      toast.success("Order cancelled successfully")
      refresh()
      if (selectedOrderId === orderId) {
        setDetailsOpen(false)
        setSelectedOrderId(null)
      }
    } catch (cancelError) {
      console.error("Order cancellation failed", cancelError)
      toast.error("Failed to cancel order")
    } finally {
      setCancellingOrderId(null)
    }
  }

  const summaryCards = [
    {
      title: "Processed",
      value: `${orders.summary.processedOrders}`,
      description: "Orders matching the active filters.",
      icon: PackageSearch,
      accent: "bg-indigo-50 text-indigo-700",
    },
    {
      title: "Open Issues",
      value: `${orders.summary.openIssues}`,
      description: "Cancelled or failed tenant orders.",
      icon: AlertTriangle,
      accent: "bg-rose-50 text-rose-700",
    },
    {
      title: "Delivered",
      value: `${orders.summary.deliveredOrders}`,
      description: "Delivered orders in the current result set.",
      icon: PackageCheck,
      accent: "bg-emerald-50 text-emerald-700",
    },
    {
      title: "Active",
      value: `${orders.summary.activeOrders}`,
      description: "Created, assigned, picked up, or in transit.",
      icon: Truck,
      accent: "bg-sky-50 text-sky-700",
    },
  ]

  return (
    <div className="mx-auto flex w-full max-w-7xl flex-col gap-6 px-4 py-6 sm:px-6 lg:px-8">
      {error ? (
        <div className="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">{error}</div>
      ) : null}

      <section>
        <Card className="overflow-hidden border-slate-200 bg-[radial-gradient(circle_at_top_left,_rgba(99,102,241,0.14),_transparent_30%),linear-gradient(135deg,#ffffff,#f8fafc)] shadow-sm">
          <CardContent className="flex flex-col gap-6 px-6 py-8 sm:px-8">
            <Badge variant="outline" className="w-fit rounded-full border-indigo-200 bg-white/80 px-3 py-1 text-indigo-700">
              Live Tenant Orders
            </Badge>
            <div className="flex flex-col gap-5 lg:flex-row lg:items-end lg:justify-between">
              <div className="space-y-2">
                <h1 className="text-3xl font-semibold tracking-tight text-slate-950">Monitor your tenant deliveries</h1>
                <p className="max-w-2xl text-sm leading-6 text-slate-600">
                  Search tenant orders by order number, provider, or address details. Filter by created date range and
                  page through live results from OrderService.
                </p>
              </div>
              <Button asChild className="rounded-full bg-slate-950 text-white hover:bg-slate-800">
                <Link href={createOrderHref}>
                  <PlusCircle className="h-4 w-4" />
                  Create Order
                </Link>
              </Button>
            </div>
          </CardContent>
        </Card>
      </section>

      <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {summaryCards.map((card) => (
          <SummaryCard key={card.title} {...card} loading={loading} />
        ))}
      </section>

      <Card className="border-slate-200 bg-white shadow-sm">
        <CardHeader className="gap-4 border-b border-slate-100 pb-5">
          <div className="flex flex-col gap-4 xl:flex-row xl:items-end xl:justify-between">
            <div>
              <CardTitle className="text-slate-950">Order List</CardTitle>
              <CardDescription>Tenant-scoped search, date filtering, and pagination.</CardDescription>
            </div>
            <div className="flex flex-wrap gap-2">
              <Button variant="outline" size="sm" className="rounded-full border-slate-300 bg-white">
                <Filter className="h-4 w-4" />
                Filtered
              </Button>
              <Button variant="outline" size="sm" className="rounded-full border-slate-300 bg-white">
                <Download className="h-4 w-4" />
                Export
              </Button>
            </div>
          </div>

          <div className="grid gap-3 lg:grid-cols-[minmax(0,1fr)_auto_auto]">
            <div className="relative">
              <Search className="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
              <Input
                className="rounded-full border-slate-200 bg-slate-50 pl-11"
                placeholder="Search order id, reference, provider, city, state, pincode..."
                value={query}
                onChange={(event) => setQuery(event.target.value)}
              />
            </div>
            <div className="flex items-center gap-2 rounded-full border border-slate-200 bg-slate-50 px-4 py-2">
              <CalendarDays className="h-4 w-4 text-slate-400" />
              <Input
                type="date"
                value={dateRange.startDate}
                onChange={(event) =>
                  setDateRange((current) => ({
                    ...current,
                    startDate: event.target.value,
                  }))
                }
                className="h-auto border-none bg-transparent p-0 shadow-none focus-visible:ring-0"
              />
            </div>
            <div className="flex items-center gap-2 rounded-full border border-slate-200 bg-slate-50 px-4 py-2">
              <Clock3 className="h-4 w-4 text-slate-400" />
              <Input
                type="date"
                value={dateRange.endDate}
                onChange={(event) =>
                  setDateRange((current) => ({
                    ...current,
                    endDate: event.target.value,
                  }))
                }
                className="h-auto border-none bg-transparent p-0 shadow-none focus-visible:ring-0"
              />
            </div>
          </div>
        </CardHeader>

        <CardContent className="pt-6">
          {loading ? (
            <div className="space-y-3">
              {Array.from({ length: 8 }, (_, index) => (
                <div key={index} className="grid gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4 lg:grid-cols-6">
                  <Skeleton className="h-5 w-28" />
                  <Skeleton className="h-5 w-36" />
                  <Skeleton className="h-5 w-32" />
                  <Skeleton className="h-5 w-24" />
                  <Skeleton className="h-5 w-24" />
                  <Skeleton className="h-5 w-24" />
                </div>
              ))}
            </div>
          ) : (
            <div className="overflow-hidden rounded-3xl border border-slate-200">
              <div className="overflow-x-auto">
                <table className="min-w-[1080px] w-full text-left">
                  <thead className="bg-slate-50">
                    <tr>
                      <th className="px-4 py-3 text-xs font-medium uppercase tracking-[0.18em] text-slate-500">Tracking ID</th>
                      <th className="px-4 py-3 text-xs font-medium uppercase tracking-[0.18em] text-slate-500">Reference</th>
                      <th className="px-4 py-3 text-xs font-medium uppercase tracking-[0.18em] text-slate-500">Route</th>
                      <th className="px-4 py-3 text-xs font-medium uppercase tracking-[0.18em] text-slate-500">Operator</th>
                      <th className="px-4 py-3 text-xs font-medium uppercase tracking-[0.18em] text-slate-500">Status</th>
                      <th className="px-4 py-3 text-xs font-medium uppercase tracking-[0.18em] text-slate-500">Created</th>
                      <th className="px-4 py-3 text-xs font-medium uppercase tracking-[0.18em] text-slate-500">Action</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-200 bg-white">
                    {orders.items.map((order) => (
                      <tr
                        key={order.id}
                        className="cursor-pointer transition hover:bg-slate-50"
                        onClick={() => openOrderDetails(order.id)}
                      >
                        <td className="px-4 py-4">
                          <div className="flex items-center gap-3">
                            <div className="rounded-xl bg-slate-100 p-2 text-slate-700">
                              <PackageSearch className="h-4 w-4" />
                            </div>
                            <span className="text-sm font-medium text-slate-950">#{order.id.slice(0, 8).toUpperCase()}</span>
                          </div>
                        </td>
                        <td className="px-4 py-4 text-sm text-slate-700">{order.customerReferenceId || "No reference"}</td>
                        <td className="px-4 py-4">
                          <div className="flex items-center gap-2 text-sm text-slate-600">
                            <MapPin className="h-4 w-4 text-slate-400" />
                            {(order.pickupCity || "Unknown pickup") + " -> " + (order.dropoffCity || "Unknown drop")}
                          </div>
                        </td>
                        <td className="px-4 py-4 text-sm text-slate-700">{order.operator || "Unassigned"}</td>
                        <td className="px-4 py-4">
                          <StatusBadge status={order.orderStatus} />
                        </td>
                        <td className="px-4 py-4 text-sm text-slate-500">{formatDate(order.createdAt)}</td>
                        <td className="px-4 py-4">
                          {canCancelOrder(order.orderStatus) ? (
                            <Button
                              size="sm"
                              variant="outline"
                              className="rounded-full border-rose-200 bg-white text-rose-700 hover:bg-rose-50 hover:text-rose-800"
                              disabled={cancellingOrderId === order.id}
                              onClick={(event) => {
                                event.stopPropagation()
                                void handleCancelOrder(order.id)
                              }}
                            >
                              {cancellingOrderId === order.id ? (
                                <Loader2 className="h-4 w-4 animate-spin" />
                              ) : (
                                <CircleSlash className="h-4 w-4" />
                              )}
                              Cancel
                            </Button>
                          ) : (
                            <span className="text-xs font-medium uppercase tracking-[0.16em] text-slate-400">
                              Not cancellable
                            </span>
                          )}
                        </td>
                      </tr>
                    ))}
                    {orders.items.length === 0 ? (
                      <tr>
                        <td colSpan={7} className="px-4 py-12 text-center text-sm text-slate-500">
                          No tenant orders matched the current search or date range.
                        </td>
                      </tr>
                    ) : null}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          <div className="mt-5 flex flex-col gap-3 text-sm text-slate-500 lg:flex-row lg:items-center lg:justify-between">
            <span>
              {loading
                ? "Loading tenant orders..."
                : `Showing ${orders.items.length} of ${pagination.totalElements} tenant orders`}
            </span>
            <div className="flex flex-wrap items-center gap-2">
              <button
                className="inline-flex h-9 w-9 items-center justify-center rounded-full border border-slate-200 bg-white text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40"
                disabled={loading || page === 0}
                onClick={() => setPage(Math.max(page - 1, 0))}
              >
                <ChevronLeft className="h-4 w-4" />
              </button>
              {Array.from({ length: pagination.totalPages }, (_, index) => index)
                .slice(Math.max(page - 2, 0), Math.max(page - 2, 0) + 5)
                .map((pageNumber) => (
                  <button
                    key={pageNumber}
                    className={`inline-flex h-9 w-9 items-center justify-center rounded-full border text-sm font-medium transition ${
                      pageNumber === page
                        ? "border-slate-950 bg-slate-950 text-white"
                        : "border-slate-200 bg-white text-slate-700 hover:bg-slate-50"
                    }`}
                    disabled={loading}
                    onClick={() => setPage(pageNumber)}
                  >
                    {pageNumber + 1}
                  </button>
                ))}
              <button
                className="inline-flex h-9 w-9 items-center justify-center rounded-full border border-slate-200 bg-white text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40"
                disabled={loading || page >= pagination.totalPages - 1 || pagination.totalPages === 0}
                onClick={() => setPage(Math.min(page + 1, pagination.totalPages - 1))}
              >
                <ChevronRight className="h-4 w-4" />
              </button>
            </div>
          </div>
        </CardContent>
      </Card>

      <TenantOrderDetailsSheet
        open={detailsOpen}
        onOpenChange={(open) => {
          setDetailsOpen(open)
          if (!open) {
            setSelectedOrderId(null)
          }
        }}
        details={orderDetails}
        tracking={orderTracking}
        loading={detailsLoading}
        error={detailsError}
      />
    </div>
  )
}

function SummaryCard({
  title,
  value,
  description,
  icon: Icon,
  accent,
  loading,
}: {
  title: string
  value: string
  description: string
  icon: ComponentType<{ className?: string }>
  accent: string
  loading: boolean
}) {
  return (
    <Card className="border-slate-200 bg-white shadow-sm">
      <CardContent className="flex items-start gap-4 px-5 py-5">
        <div className={`rounded-2xl p-3 ${accent}`}>
          <Icon className="h-5 w-5" />
        </div>
        <div className="space-y-2">
          <p className="text-xs font-medium uppercase tracking-[0.2em] text-slate-500">{title}</p>
          {loading ? <Skeleton className="h-8 w-20" /> : <p className="text-3xl font-semibold text-slate-950">{value}</p>}
          <p className="text-sm leading-6 text-slate-500">{description}</p>
        </div>
      </CardContent>
    </Card>
  )
}

function StatusBadge({ status }: { status: string }) {
  const tone = statusTone[status as keyof typeof statusTone] || "bg-slate-100 text-slate-700"
  return <span className={`inline-flex rounded-full px-3 py-1 text-xs font-medium ${tone}`}>{status}</span>
}

function Skeleton({ className }: { className?: string }) {
  return <div className={`animate-pulse rounded-xl bg-slate-200 ${className || ""}`.trim()} />
}

function formatDate(date: string) {
  return new Intl.DateTimeFormat("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  }).format(new Date(date))
}

function canCancelOrder(status: string) {
  return status === "CREATED" || status === "ASSIGNED"
}
