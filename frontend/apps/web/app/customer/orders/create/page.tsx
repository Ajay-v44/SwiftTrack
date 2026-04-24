"use client"

import Link from "next/link"
import { useEffect, useMemo, useState } from "react"
import { toast } from "sonner"
import { CheckCircle2, PackagePlus, RefreshCw } from "lucide-react"
import {
  createConsumerOrderService,
  fetchConsumerOrderQuotesService,
  fetchTenantAddressesService,
} from "@swifttrack/services"
import type { CustomerDeliveryOptionsQuote, TenantSavedAddress } from "@swifttrack/types"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"

export default function CustomerCreateOrderPage() {
  const [addresses, setAddresses] = useState<TenantSavedAddress[]>([])
  const [pickupAddressId, setPickupAddressId] = useState("")
  const [dropAddressId, setDropAddressId] = useState("")
  const [orderReference, setOrderReference] = useState(`CUST-${Date.now().toString().slice(-6)}`)
  const [description, setDescription] = useState("")
  const [weightKg, setWeightKg] = useState("1")
  const [quote, setQuote] = useState<CustomerDeliveryOptionsQuote | null>(null)
  const [selectedQuoteId, setSelectedQuoteId] = useState("")
  const [loadingQuote, setLoadingQuote] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  const pickup = useMemo(() => addresses.find((item) => item.id === pickupAddressId) ?? null, [addresses, pickupAddressId])
  const drop = useMemo(() => addresses.find((item) => item.id === dropAddressId) ?? null, [addresses, dropAddressId])

  useEffect(() => {
    fetchTenantAddressesService()
      .then((items) => {
        setAddresses(items)
        setPickupAddressId(items[0]?.id ?? "")
        setDropAddressId(items.find((item) => item.id !== items[0]?.id)?.id ?? "")
      })
      .catch(() => toast.error("Failed to load addresses"))
  }, [])

  async function refreshQuote() {
    if (!pickup || !drop || drop.id === pickup.id || drop.latitude == null || drop.longitude == null) {
      toast.error("Select different pickup and drop addresses")
      return
    }
    setLoadingQuote(true)
    try {
      const nextQuote = await fetchConsumerOrderQuotesService({
        pickupAddressId: pickup.id,
        dropoffLat: drop.latitude,
        dropoffLng: drop.longitude,
      })
      setQuote(nextQuote)
      setSelectedQuoteId(nextQuote.options[0]?.quoteOptionId ?? "")
    } catch {
      toast.error("No delivery options available")
    } finally {
      setLoadingQuote(false)
    }
  }

  async function createOrder() {
    if (!pickup || !drop || !quote || !selectedQuoteId || !description.trim()) {
      toast.error("Select addresses, quote option, and package description")
      return
    }
    setSubmitting(true)
    try {
      const response = await createConsumerOrderService({
        quoteSessionId: quote.quoteSessionId,
        selectedQuoteId,
        orderReference,
        paymentType: "PREPAID",
        pickupAddressId: pickup.id,
        dropoff: {
          addressId: drop.id,
          line1: drop.line1,
          line2: drop.line2,
          city: drop.city,
          state: drop.state,
          country: drop.country,
          pincode: drop.pincode,
          locality: drop.locality,
          latitude: drop.latitude ?? 0,
          longitude: drop.longitude ?? 0,
          contactName: drop.contactName,
          contactPhone: drop.contactPhone,
          businessName: drop.businessName,
          notes: drop.notes,
        },
        packageInfo: {
          totalValue: null,
          totalWeightGrams: Math.round(Number(weightKg) * 1000),
          size: "SMALL",
          description: description.trim(),
        },
        deliveryInstructions: null,
      })
      toast.success("Shipment created")
      window.location.href = `/track?id=${response.orderId}`
    } catch {
      toast.error("Failed to create shipment")
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="mx-auto grid max-w-7xl gap-6 px-4 py-6 sm:px-6 lg:grid-cols-[1fr_0.8fr] lg:px-8">
      <Card className="border-slate-200 bg-white shadow-sm">
        <CardHeader><CardTitle>Create customer shipment</CardTitle></CardHeader>
        <CardContent className="space-y-4">
          {addresses.length < 2 ? (
            <div className="rounded-2xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-800">
              Add at least two addresses before creating a shipment. <Link className="font-medium underline" href="/customer/addresses">Open addresses</Link>
            </div>
          ) : null}
          <SelectAddress label="Pickup address" value={pickupAddressId} addresses={addresses} onChange={setPickupAddressId} />
          <SelectAddress label="Drop address" value={dropAddressId} addresses={addresses.filter((item) => item.id !== pickupAddressId)} onChange={setDropAddressId} />
          <label className="grid gap-1 text-sm text-slate-600">Order reference<Input value={orderReference} onChange={(event) => setOrderReference(event.target.value)} /></label>
          <label className="grid gap-1 text-sm text-slate-600">Package description<Input value={description} onChange={(event) => setDescription(event.target.value)} /></label>
          <label className="grid gap-1 text-sm text-slate-600">Weight kg<Input type="number" value={weightKg} onChange={(event) => setWeightKg(event.target.value)} /></label>
          <Button className="rounded-full bg-slate-950 text-white hover:bg-slate-800" disabled={loadingQuote} onClick={refreshQuote}>
            <RefreshCw className={`h-4 w-4 ${loadingQuote ? "animate-spin" : ""}`} /> Get delivery options
          </Button>
        </CardContent>
      </Card>

      <Card className="border-slate-200 bg-white shadow-sm">
        <CardHeader><CardTitle>Select provider</CardTitle></CardHeader>
        <CardContent className="space-y-3">
          {!quote ? <p className="text-sm text-slate-500">Get delivery options after selecting addresses.</p> : null}
          {quote?.options.map((option) => (
            <button
              key={option.quoteOptionId}
              className={`w-full rounded-2xl border p-4 text-left ${selectedQuoteId === option.quoteOptionId ? "border-slate-950 bg-slate-950 text-white" : "border-slate-200 bg-white"}`}
              onClick={() => setSelectedQuoteId(option.quoteOptionId)}
            >
              <p className="font-medium">{option.choiceCode || option.providerCode || "SwiftTrack Driver"}</p>
              <p className="mt-1 text-sm opacity-80">{option.currency || "INR"} {option.price.toFixed(2)} · {option.selectedType}</p>
            </button>
          ))}
          <Button className="w-full rounded-full bg-emerald-600 text-white hover:bg-emerald-700" disabled={!selectedQuoteId || submitting} onClick={createOrder}>
            {submitting ? <PackagePlus className="h-4 w-4 animate-pulse" /> : <CheckCircle2 className="h-4 w-4" />}
            Create shipment
          </Button>
        </CardContent>
      </Card>
    </div>
  )
}

function SelectAddress({ label, value, addresses, onChange }: { label: string; value: string; addresses: TenantSavedAddress[]; onChange: (value: string) => void }) {
  return (
    <label className="grid gap-1 text-sm text-slate-600">
      {label}
      <select className="h-10 rounded-md border border-slate-200 bg-slate-50 px-3 text-sm" value={value} onChange={(event) => onChange(event.target.value)}>
        <option value="">Select address</option>
        {addresses.map((address) => (
          <option key={address.id} value={address.id}>{address.label || address.line1}</option>
        ))}
      </select>
    </label>
  )
}
