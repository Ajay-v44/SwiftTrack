"use client"

import {
  Activity,
  CreditCard,
  Dot,
  MapPinned,
  PackageSearch,
  ShieldCheck,
  TimerReset,
  UserRound,
} from "lucide-react"
import type {
  TenantOrderDetailsResponse,
  TenantOrderTrackingResponse,
} from "@swifttrack/types"
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet"
import { Badge } from "@/components/ui/badge"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { TenantOrderMap } from "@/components/tenant/TenantOrderMap"

const statusTone: Record<string, string> = {
  CREATED: "bg-violet-50 text-violet-700 border-violet-200",
  QUOTED: "bg-fuchsia-50 text-fuchsia-700 border-fuchsia-200",
  ASSIGNED: "bg-indigo-50 text-indigo-700 border-indigo-200",
  PICKED_UP: "bg-sky-50 text-sky-700 border-sky-200",
  IN_TRANSIT: "bg-cyan-50 text-cyan-700 border-cyan-200",
  OUT_FOR_DELIVERY: "bg-amber-50 text-amber-700 border-amber-200",
  DELIVERED: "bg-emerald-50 text-emerald-700 border-emerald-200",
  CANCELLED: "bg-rose-50 text-rose-700 border-rose-200",
  FAILED: "bg-rose-50 text-rose-700 border-rose-200",
}

function formatDateTime(value: string | null | undefined) {
  if (!value) {
    return "Not available"
  }

  return new Intl.DateTimeFormat("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(value))
}

function formatMoney(value: number | null | undefined) {
  if (typeof value !== "number") {
    return "Not available"
  }

  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    minimumFractionDigits: 2,
  }).format(value)
}

function formatLocationLine(city?: string | null, state?: string | null, locality?: string | null) {
  return [locality, city, state].filter(Boolean).join(", ") || "Location unavailable"
}

export function TenantOrderDetailsSheet({
  open,
  onOpenChange,
  details,
  tracking,
  loading,
  error,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
  details: TenantOrderDetailsResponse | null
  tracking: TenantOrderTrackingResponse | null
  loading: boolean
  error: string | null
}) {
  const timeline = tracking?.trackingHistory ?? []
  const currentLocation = tracking?.currentLocation || details?.currentLocation || null
  const trackingStatus = tracking?.trackingStatus || details?.trackingStatus || details?.orderStatus || "Unknown"
  const statusToneClass = statusTone[trackingStatus] || "bg-slate-100 text-slate-700 border-slate-200"
  const routeConfidence = currentLocation ? "Live signal available" : "Waiting for live signal"
  const routePhase = currentLocation ? "Vehicle on route" : "Route staged"

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent
        side="right"
        className="w-full gap-0 overflow-y-auto border-l border-slate-200 bg-slate-50 p-0 sm:max-w-4xl"
      >
        <SheetHeader className="border-b border-slate-200 bg-white px-6 py-5">
          <div className="flex flex-wrap items-start justify-between gap-3 pr-10">
            <div className="space-y-2">
              <Badge variant="outline" className={`rounded-full ${statusToneClass}`}>
                {trackingStatus}
              </Badge>
              <SheetTitle className="text-2xl text-slate-950">
                {details?.customerReferenceId || (details ? `Order ${details.id.slice(0, 8).toUpperCase()}` : "Order details")}
              </SheetTitle>
              <SheetDescription>
                Full route, status history, provider context, and latest tracked order position.
              </SheetDescription>
            </div>
            {details ? (
              <div className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-right">
                <p className="text-[11px] uppercase tracking-[0.18em] text-slate-500">Order ID</p>
                <p className="mt-1 font-mono text-sm text-slate-900">{details.id}</p>
              </div>
            ) : null}
          </div>
        </SheetHeader>

        <div className="space-y-5 px-6 py-6">
          {loading ? (
            <div className="rounded-2xl border border-slate-200 bg-white px-4 py-8 text-sm text-slate-500">
              Loading order analytics...
            </div>
          ) : details ? (
            <>
              {error ? (
                <div className="rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">
                  {error}
                </div>
              ) : null}
              <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
                <InfoCard
                  icon={PackageSearch}
                  label="Current State"
                  value={trackingStatus}
                  helper={`Order lifecycle: ${details.orderStatus}`}
                />
                <InfoCard
                  icon={TimerReset}
                  label="Route Phase"
                  value={routePhase}
                  helper={routeConfidence}
                />
                <InfoCard
                  icon={Activity}
                  label="Last GPS Update"
                  value={formatDateTime(tracking?.lastLocationUpdatedAt || details.lastLocationUpdatedAt)}
                  helper={currentLocation ? "Latest tracked order position available" : "No tracked position yet"}
                />
                <InfoCard
                  icon={ShieldCheck}
                  label="Dispatch Source"
                  value={details.selectedProviderCode || "Internal"}
                  helper={details.assignedDriverId ? `Driver ${details.assignedDriverId.slice(0, 8)}` : "Driver pending"}
                />
              </div>

              <Card className="border-slate-200 bg-white shadow-sm">
                <CardHeader className="border-b border-slate-100 pb-4">
                  <div className="flex flex-wrap items-start justify-between gap-3">
                    <CardTitle className="flex items-center gap-2 text-lg text-slate-950">
                      <MapPinned className="h-5 w-5 text-slate-700" />
                      Live Tracking
                    </CardTitle>
                    <div className="flex flex-wrap items-center gap-2 rounded-full border border-slate-200 bg-slate-50 px-3 py-1.5 text-xs text-slate-600">
                      <Dot className="h-4 w-4 text-emerald-500" />
                      Tracking focus: pickup, live position, dropoff
                    </div>
                  </div>
                </CardHeader>
                <CardContent className="space-y-5 pt-5">
                  <TenantOrderMap
                    pickup={details.pickup}
                    dropoff={details.dropoff}
                    driverLocation={currentLocation}
                    trackingStatus={trackingStatus}
                  />
                  <div className="grid gap-3 xl:grid-cols-[minmax(0,1.2fr)_minmax(320px,0.8fr)]">
                    <div className="grid gap-3 md:grid-cols-3">
                      <RouteCard
                        title="Pickup"
                        subtitle={formatLocationLine(
                          details.pickup?.city,
                          details.pickup?.state,
                          details.pickup?.locality
                        )}
                        meta={formatCoordinates(details.pickup?.latitude, details.pickup?.longitude)}
                        tone="teal"
                      />
                      <RouteCard
                        title="Live Position"
                        subtitle={
                          currentLocation
                            ? formatCoordinates(currentLocation.latitude, currentLocation.longitude)
                            : "No tracked position yet"
                        }
                        meta={formatDateTime(currentLocation?.updatedAt)}
                        tone="amber"
                      />
                      <RouteCard
                        title="Dropoff"
                        subtitle={formatLocationLine(
                          details.dropoff?.city,
                          details.dropoff?.state,
                          details.dropoff?.locality
                        )}
                        meta={formatCoordinates(details.dropoff?.latitude, details.dropoff?.longitude)}
                        tone="blue"
                      />
                    </div>
                    <div className="rounded-3xl border border-slate-200 bg-slate-950 p-5 text-white shadow-sm">
                      <p className="text-[11px] uppercase tracking-[0.22em] text-slate-400">Tracking Summary</p>
                      <p className="mt-3 text-lg font-semibold text-white">{trackingStatus}</p>
                      <div className="mt-4 space-y-3 text-sm text-slate-300">
                        <SummaryRow label="Order Created" value={formatDateTime(details.createdAt)} />
                        <SummaryRow
                          label="Status Updated"
                          value={formatDateTime(tracking?.lastStatusUpdatedAt || details.lastStatusUpdatedAt)}
                        />
                        <SummaryRow
                          label="Location Updated"
                          value={formatDateTime(tracking?.lastLocationUpdatedAt || details.lastLocationUpdatedAt)}
                        />
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <div className="grid items-start gap-5 xl:grid-cols-[minmax(0,1.2fr)_minmax(320px,0.8fr)]">
                <Card className="border-slate-200 bg-white shadow-sm">
                  <CardHeader className="border-b border-slate-100 pb-4">
                    <CardTitle className="text-lg text-slate-950">Tracking Timeline</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-4 pt-5">
                    {timeline.length > 0 ? (
                      timeline
                        .slice()
                        .reverse()
                        .map((event) => (
                          <div key={event.id} className="flex gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4">
                            <div className="mt-1.5 flex h-3 w-3 shrink-0 rounded-full bg-slate-900 ring-4 ring-slate-200/80" />
                            <div className="min-w-0 space-y-1">
                              <div className="flex flex-wrap items-center gap-2">
                                <Badge variant="outline" className={`rounded-full ${statusTone[event.status || ""] || "bg-slate-100 text-slate-700 border-slate-200"}`}>
                                  {event.status || "UPDATE"}
                                </Badge>
                                <span className="text-xs text-slate-500">{formatDateTime(event.eventTime || event.createdAt)}</span>
                              </div>
                              <p className="text-sm font-medium text-slate-900">{event.description || "Status updated from provider feed"}</p>
                              <p className="text-xs text-slate-500">
                                Provider: {event.providerCode || details.selectedProviderCode || "Internal"}
                              </p>
                              <p className="text-xs text-slate-500">{formatCoordinates(event.latitude, event.longitude)}</p>
                            </div>
                          </div>
                        ))
                    ) : (
                      <div className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-6 text-sm text-slate-500">
                        No tracking events have been recorded for this order yet.
                      </div>
                    )}
                  </CardContent>
                </Card>

                <div className="space-y-5">
                  <Card className="border-slate-200 bg-white shadow-sm">
                    <CardHeader className="border-b border-slate-100 pb-4">
                      <CardTitle className="flex items-center gap-2 text-lg text-slate-950">
                        <CreditCard className="h-5 w-5 text-slate-700" />
                        Tenant Debit
                      </CardTitle>
                    </CardHeader>
                    <CardContent className="grid gap-3 pt-5">
                      <MetaRow
                        label="Debited Amount"
                        value={formatMoney(details.tenantDebit?.debitedAmount)}
                      />
                      <MetaRow
                        label="Last Debited At"
                        value={formatDateTime(details.tenantDebit?.lastDebitedAt)}
                      />
                      <MetaRow
                        label="Ledger Account"
                        value={details.tenantDebit?.accountId || "Not available"}
                      />
                      <MetaRow
                        label="Ledger Note"
                        value={details.tenantDebit?.description || "Not available"}
                      />
                    </CardContent>
                  </Card>

                  <Card className="border-slate-200 bg-white shadow-sm">
                  <CardHeader className="border-b border-slate-100 pb-4">
                    <CardTitle className="flex items-center gap-2 text-lg text-slate-950">
                      <UserRound className="h-5 w-5 text-slate-700" />
                      Order Analytics
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="grid gap-3 pt-5">
                    <MetaRow label="Access Scope" value={details.accessScope || "Not available"} />
                    <MetaRow label="Booking Channel" value={details.bookingChannel || "Not available"} />
                    <MetaRow label="Order Type" value={details.orderType || "Not available"} />
                    <MetaRow label="Payment Type" value={details.paymentType || "Not available"} />
                    <MetaRow label="Payment Amount" value={formatMoney(details.paymentAmount)} />
                    <MetaRow label="Provider Code" value={details.selectedProviderCode || "Not available"} />
                    <MetaRow label="Provider Order ID" value={details.providerOrderId || "Not available"} />
                    <MetaRow label="Quote Session" value={details.quoteSessionId || "Not available"} />
                    <MetaRow label="Selected Type" value={details.selectedType || "Not available"} />
                    <MetaRow label="Tenant ID" value={details.tenantId || "Not available"} />
                    <MetaRow label="Created By" value={details.createdBy || "Not available"} />
                    <MetaRow label="Owner User" value={details.ownerUserId || "Not available"} />
                    <MetaRow label="Updated At" value={formatDateTime(details.updatedAt)} />
                  </CardContent>
                  </Card>
                </div>
              </div>
            </>
          ) : (
            <>
              {error ? (
                <div className="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">{error}</div>
              ) : null}
              <div className="rounded-2xl border border-slate-200 bg-white px-4 py-8 text-sm text-slate-500">
                Select an order to view details.
              </div>
            </>
          )}
        </div>
      </SheetContent>
    </Sheet>
  )
}

function InfoCard({
  icon: Icon,
  label,
  value,
  helper,
}: {
  icon: typeof PackageSearch
  label: string
  value: string
  helper: string
}) {
  return (
    <div className="h-full rounded-3xl border border-slate-200 bg-white p-4 shadow-sm">
      <div className="flex items-start gap-3">
        <div className="rounded-2xl bg-slate-100 p-3 text-slate-700">
          <Icon className="h-5 w-5" />
        </div>
        <div className="min-w-0 space-y-1">
          <p className="text-[11px] uppercase tracking-[0.18em] text-slate-500">{label}</p>
          <p className="text-sm font-semibold text-slate-950">{value}</p>
          <p className="text-xs leading-5 text-slate-500">{helper}</p>
        </div>
      </div>
    </div>
  )
}

function RouteCard({
  title,
  subtitle,
  meta,
  tone,
}: {
  title: string
  subtitle: string
  meta: string
  tone: "teal" | "amber" | "blue"
}) {
  const toneClass = {
    teal: "border-teal-100 bg-teal-50/70",
    amber: "border-amber-100 bg-amber-50/70",
    blue: "border-blue-100 bg-blue-50/70",
  }

  return (
    <div className={`rounded-2xl border p-4 ${toneClass[tone]}`}>
      <p className="text-[11px] uppercase tracking-[0.18em] text-slate-500">{title}</p>
      <p className="mt-2 text-sm font-medium text-slate-900">{subtitle}</p>
      <p className="mt-1 text-xs text-slate-500">{meta}</p>
    </div>
  )
}

function SummaryRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-center justify-between gap-3 border-b border-white/8 pb-3 last:border-b-0 last:pb-0">
      <span className="text-slate-400">{label}</span>
      <span className="text-right font-medium text-white">{value}</span>
    </div>
  )
}

function MetaRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="grid grid-cols-[minmax(0,1fr)_minmax(0,1.2fr)] items-start gap-4 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3">
      <span className="text-sm text-slate-500">{label}</span>
      <span className="break-all text-right text-sm font-medium text-slate-900">{value}</span>
    </div>
  )
}

function formatCoordinates(latitude?: number | null, longitude?: number | null) {
  if (typeof latitude !== "number" || typeof longitude !== "number") {
    return "Coordinates unavailable"
  }

  return `${latitude.toFixed(5)}, ${longitude.toFixed(5)}`
}
