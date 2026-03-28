"use client"

import { useMemo, useState } from "react"
import Link from "next/link"
import { AlertTriangle, Download, Filter, MapPin, PackageOpen, PackageSearch, PlusCircle, Search, Truck } from "lucide-react"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"

const MOCK_ORDERS = [
  { id: "#TRK-49201A", dest: "Dallas, TX", operator: "DHL Express", status: "In Transit", time: "14:30 EST", eta: "Oct 25, 2024" },
  { id: "#TRK-49202B", dest: "Seattle, WA", operator: "Internal Fleet", status: "Delivered", time: "09:15 PST", eta: "Oct 24, 2024" },
  { id: "#TRK-49203C", dest: "Miami, FL", operator: "FedEx", status: "Exception", time: "11:45 EST", eta: "Delayed" },
  { id: "#TRK-49204D", dest: "New York, NY", operator: "Local Express", status: "Processing", time: "16:00 EST", eta: "Oct 26, 2024" },
  { id: "#TRK-49205E", dest: "Chicago, IL", operator: "DHL Express", status: "In Transit", time: "10:20 CST", eta: "Oct 25, 2024" },
]

export default function TenantOrdersPage() {
  const [searchTerm, setSearchTerm] = useState("")

  const filteredOrders = useMemo(
    () =>
      MOCK_ORDERS.filter(
        (order) =>
          order.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
          order.dest.toLowerCase().includes(searchTerm.toLowerCase()) ||
          order.operator.toLowerCase().includes(searchTerm.toLowerCase())
      ),
    [searchTerm]
  )

  return (
    <div className="mx-auto flex w-full max-w-7xl flex-col gap-6 px-4 py-6 sm:px-6 lg:px-8">
      <section className="grid gap-6 xl:grid-cols-[minmax(0,1.45fr)_minmax(280px,0.9fr)]">
        <Card className="overflow-hidden border-slate-200 bg-[radial-gradient(circle_at_top_left,_rgba(99,102,241,0.14),_transparent_30%),linear-gradient(135deg,#ffffff,#f8fafc)] shadow-sm">
          <CardContent className="flex flex-col gap-6 px-6 py-8 sm:px-8">
            <Badge variant="outline" className="w-fit rounded-full border-indigo-200 bg-white/80 px-3 py-1 text-indigo-700">
              Order Command
            </Badge>
            <div className="flex flex-col gap-5 lg:flex-row lg:items-end lg:justify-between">
              <div className="space-y-2">
                <h1 className="text-3xl font-semibold tracking-tight text-slate-950">Monitor your tenant deliveries</h1>
                <p className="max-w-2xl text-sm leading-6 text-slate-600">
                  Search active orders, inspect operational exceptions, and keep dispatch visibility inside the same tenant workspace.
                </p>
              </div>
              <Button asChild className="rounded-full bg-slate-950 text-white hover:bg-slate-800">
                <Link href="/tenant/orders/create">
                  <PlusCircle className="h-4 w-4" />
                  Create Order
                </Link>
              </Button>
            </div>
          </CardContent>
        </Card>

        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-1">
          <SummaryCard
            title="Processed"
            value="543"
            description="Orders processed in the current working set."
            icon={PackageOpen}
            accent="bg-emerald-50 text-emerald-700"
          />
          <SummaryCard
            title="Open Issues"
            value="12"
            description="Orders currently blocked or delayed."
            icon={AlertTriangle}
            accent="bg-rose-50 text-rose-700"
          />
        </div>
      </section>

      <Card className="border-slate-200 bg-white shadow-sm">
        <CardHeader className="gap-4 border-b border-slate-100 pb-5">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
            <div>
              <CardTitle className="text-slate-950">Orders</CardTitle>
              <CardDescription>
                This page is still using placeholder list data until the tenant orders list API is wired.
              </CardDescription>
            </div>
            <div className="flex flex-wrap gap-2">
              <Button variant="outline" size="sm" className="rounded-full border-slate-300 bg-white">
                <Filter className="h-4 w-4" />
                Filter Status
              </Button>
              <Button variant="outline" size="sm" className="rounded-full border-slate-300 bg-white">
                <Download className="h-4 w-4" />
                Export
              </Button>
            </div>
          </div>

          <div className="relative max-w-md">
            <Search className="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <Input
              className="rounded-full border-slate-200 bg-slate-50 pl-11"
              placeholder="Search by ID, destination, or operator"
              value={searchTerm}
              onChange={(event) => setSearchTerm(event.target.value)}
            />
          </div>
        </CardHeader>

        <CardContent className="pt-6">
          <div className="overflow-hidden rounded-3xl border border-slate-200">
            <div className="overflow-x-auto">
              <table className="min-w-[760px] w-full text-left">
                <thead className="bg-slate-50">
                  <tr>
                    <th className="px-4 py-3 text-xs font-medium uppercase tracking-[0.18em] text-slate-500">Tracking ID</th>
                    <th className="px-4 py-3 text-xs font-medium uppercase tracking-[0.18em] text-slate-500">Destination</th>
                    <th className="px-4 py-3 text-xs font-medium uppercase tracking-[0.18em] text-slate-500">Operator</th>
                    <th className="px-4 py-3 text-xs font-medium uppercase tracking-[0.18em] text-slate-500">Status</th>
                    <th className="px-4 py-3 text-xs font-medium uppercase tracking-[0.18em] text-slate-500">Last Update</th>
                    <th className="px-4 py-3 text-xs font-medium uppercase tracking-[0.18em] text-slate-500">ETA</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-200 bg-white">
                  {filteredOrders.map((order) => (
                    <tr key={order.id} className="transition hover:bg-slate-50">
                      <td className="px-4 py-4">
                        <div className="flex items-center gap-3">
                          <div className="rounded-xl bg-slate-100 p-2 text-slate-700">
                            <PackageSearch className="h-4 w-4" />
                          </div>
                          <span className="text-sm font-medium text-slate-950">{order.id}</span>
                        </div>
                      </td>
                      <td className="px-4 py-4">
                        <div className="flex items-center gap-2 text-sm text-slate-600">
                          <MapPin className="h-4 w-4 text-slate-400" />
                          {order.dest}
                        </div>
                      </td>
                      <td className="px-4 py-4 text-sm font-medium text-slate-700">{order.operator}</td>
                      <td className="px-4 py-4">
                        <StatusBadge status={order.status} />
                      </td>
                      <td className="px-4 py-4 text-sm text-slate-500">{order.time}</td>
                      <td className="px-4 py-4 text-sm font-medium text-slate-950">{order.eta}</td>
                    </tr>
                  ))}
                  {filteredOrders.length === 0 ? (
                    <tr>
                      <td colSpan={6} className="px-4 py-12 text-center text-sm text-slate-500">
                        No orders matched your search.
                      </td>
                    </tr>
                  ) : null}
                </tbody>
              </table>
            </div>
          </div>

          <div className="mt-4 flex flex-col gap-2 text-sm text-slate-500 sm:flex-row sm:items-center sm:justify-between">
            <span>Showing {filteredOrders.length} entries</span>
            <span className="inline-flex items-center gap-2 rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-700">
              <Truck className="h-3.5 w-3.5" />
              API wiring pending for full tenant order history
            </span>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}

function SummaryCard({
  title,
  value,
  description,
  icon: Icon,
  accent,
}: {
  title: string
  value: string
  description: string
  icon: React.ComponentType<{ className?: string }>
  accent: string
}) {
  return (
    <Card className="border-slate-200 bg-white shadow-sm">
      <CardContent className="flex items-start gap-4 px-5 py-5">
        <div className={`rounded-2xl p-3 ${accent}`}>
          <Icon className="h-5 w-5" />
        </div>
        <div className="space-y-2">
          <p className="text-xs font-medium uppercase tracking-[0.2em] text-slate-500">{title}</p>
          <p className="text-3xl font-semibold text-slate-950">{value}</p>
          <p className="text-sm leading-6 text-slate-500">{description}</p>
        </div>
      </CardContent>
    </Card>
  )
}

function StatusBadge({ status }: { status: string }) {
  const tone =
    status === "In Transit"
      ? "bg-sky-50 text-sky-700"
      : status === "Delivered"
        ? "bg-emerald-50 text-emerald-700"
        : status === "Exception"
          ? "bg-rose-50 text-rose-700"
          : "bg-slate-100 text-slate-700"

  return <span className={`inline-flex rounded-full px-3 py-1 text-xs font-medium ${tone}`}>{status}</span>
}
