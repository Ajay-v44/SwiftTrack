"use client"

import Link from "next/link"
import { useEffect, useState } from "react"
import { MapPinHouse, PackagePlus, Search, Truck } from "lucide-react"
import { fetchConsumerOrdersService, fetchTenantAddressesService } from "@swifttrack/services"
import type { PaginatedTenantOrdersResponse, TenantSavedAddress } from "@swifttrack/types"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"

export default function CustomerDashboardPage() {
  const [addresses, setAddresses] = useState<TenantSavedAddress[]>([])
  const [orders, setOrders] = useState<PaginatedTenantOrdersResponse | null>(null)

  useEffect(() => {
    void Promise.all([
      fetchTenantAddressesService().then(setAddresses).catch(() => setAddresses([])),
      fetchConsumerOrdersService({ page: 0, size: 5 }).then(setOrders).catch(() => setOrders(null)),
    ])
  }, [])

  return (
    <div className="mx-auto flex max-w-7xl flex-col gap-6 px-4 py-6 sm:px-6 lg:px-8">
      <section className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
        <p className="text-sm font-medium uppercase tracking-[0.2em] text-slate-500">Customer dashboard</p>
        <h1 className="mt-3 text-3xl font-semibold tracking-tight">Book, choose, and track your deliveries</h1>
        <p className="mt-3 max-w-3xl text-sm leading-6 text-slate-600">
          This workspace is for individual customers. There is no tenant setup or team configuration here.
        </p>
        <div className="mt-6 flex flex-wrap gap-3">
          <Button asChild className="rounded-full bg-slate-950 text-white hover:bg-slate-800">
            <Link href="/customer/orders/create"><PackagePlus className="h-4 w-4" /> Create shipment</Link>
          </Button>
          <Button asChild variant="outline" className="rounded-full bg-white">
            <Link href="/customer/addresses"><MapPinHouse className="h-4 w-4" /> Manage addresses</Link>
          </Button>
          <Button asChild variant="outline" className="rounded-full bg-white">
            <Link href="/track"><Search className="h-4 w-4" /> Track shipment</Link>
          </Button>
        </div>
      </section>

      <section className="grid gap-4 md:grid-cols-3">
        <Metric title="Saved addresses" value={String(addresses.length)} />
        <Metric title="Total orders" value={String(orders?.totalElements ?? 0)} />
        <Metric title="Active orders" value={String(orders?.summary.activeOrders ?? 0)} />
      </section>

      <Card className="border-slate-200 bg-white shadow-sm">
        <CardHeader><CardTitle>Recent shipments</CardTitle></CardHeader>
        <CardContent className="space-y-3">
          {(orders?.items ?? []).length === 0 ? (
            <p className="text-sm text-slate-500">No shipments yet.</p>
          ) : (
            orders!.items.map((order) => (
              <div key={order.id} className="flex items-center justify-between rounded-2xl border border-slate-200 p-4">
                <div>
                  <p className="font-medium">#{order.id.slice(0, 8).toUpperCase()}</p>
                  <p className="text-sm text-slate-500">{order.pickupCity || "Pickup"} to {order.dropoffCity || "Drop"}</p>
                </div>
                <div className="flex items-center gap-2 text-sm text-slate-600"><Truck className="h-4 w-4" /> {order.orderStatus}</div>
              </div>
            ))
          )}
        </CardContent>
      </Card>
    </div>
  )
}

function Metric({ title, value }: { title: string; value: string }) {
  return (
    <div className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm">
      <p className="text-sm text-slate-500">{title}</p>
      <p className="mt-2 text-3xl font-semibold">{value}</p>
    </div>
  )
}
