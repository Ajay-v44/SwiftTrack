"use client"

import type React from "react"
import { useEffect, useMemo, useState } from "react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { ArrowLeft, CheckCircle2, Loader2, MapPin, Package, Phone, RefreshCw, Star, Truck } from "lucide-react"
import { toast } from "sonner"
import {
  createTenantOrderService,
  fetchPlaceSuggestionsService,
  fetchTenantAddressesService,
  fetchTenantOrderQuotesService,
} from "@swifttrack/services"
import type { TenantCreateOrderInput, TenantOrderQuote, TenantPlaceSuggestion, TenantSavedAddress } from "@swifttrack/types"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"

type DropMode = "saved" | "one-time"

type AddressFormState = {
  label: string
  line1: string
  line2: string
  city: string
  state: string
  country: string
  pincode: string
  locality: string
  latitude: string
  longitude: string
  contactName: string
  contactPhone: string
  businessName: string
  notes: string
}

type ShipmentFormState = {
  orderReference: string
  paymentType: "PREPAID" | "COD"
  packageSize: "SMALL" | "MEDIUM" | "LARGE" | "XL"
  packageDescription: string
  weightKg: string
  declaredValue: string
  deliveryInstructions: string
}

const EMPTY_ADDRESS_FORM: AddressFormState = {
  label: "",
  line1: "",
  line2: "",
  city: "",
  state: "",
  country: "India",
  pincode: "",
  locality: "",
  latitude: "",
  longitude: "",
  contactName: "",
  contactPhone: "",
  businessName: "",
  notes: "",
}

const EMPTY_SHIPMENT_FORM: ShipmentFormState = {
  orderReference: "",
  paymentType: "PREPAID",
  packageSize: "SMALL",
  packageDescription: "",
  weightKg: "",
  declaredValue: "",
  deliveryInstructions: "",
}

export default function CreateOrderPage() {
  const router = useRouter()
  const [addresses, setAddresses] = useState<TenantSavedAddress[]>([])
  const [addressesLoading, setAddressesLoading] = useState(true)
  const [pickupAddressId, setPickupAddressId] = useState("")
  const [dropMode, setDropMode] = useState<DropMode>("one-time")
  const [dropSavedAddressId, setDropSavedAddressId] = useState("")
  const [dropoffForm, setDropoffForm] = useState<AddressFormState>(EMPTY_ADDRESS_FORM)
  const [shipmentForm, setShipmentForm] = useState<ShipmentFormState>(EMPTY_SHIPMENT_FORM)
  const [quote, setQuote] = useState<TenantOrderQuote | null>(null)
  const [quoteLoading, setQuoteLoading] = useState(false)
  const [quoteError, setQuoteError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const pickupAddress = useMemo(
    () => addresses.find((address) => address.id === pickupAddressId) ?? null,
    [addresses, pickupAddressId]
  )
  const savedDropAddress = useMemo(
    () => addresses.find((address) => address.id === dropSavedAddressId) ?? null,
    [addresses, dropSavedAddressId]
  )

  useEffect(() => {
    void loadAddresses()
  }, [])

  useEffect(() => {
    if (!pickupAddressId && addresses.length > 0) {
      setPickupAddressId(addresses[0].id)
    }
  }, [addresses, pickupAddressId])

  useEffect(() => {
    const activeDrop = getActiveDropoff(dropMode, savedDropAddress, dropoffForm)
    if (!pickupAddressId || !activeDrop) {
      setQuote(null)
      setQuoteError(null)
      return
    }

    let active = true
    const timer = window.setTimeout(async () => {
      setQuoteLoading(true)
      setQuoteError(null)
      try {
        const nextQuote = await fetchTenantOrderQuotesService({
          pickupAddressId,
          dropoffLat: activeDrop.latitude,
          dropoffLng: activeDrop.longitude,
        })

        if (active) {
          setQuote(nextQuote)
        }
      } catch (error) {
        console.error("Failed to fetch quote", error)
        if (active) {
          setQuote(null)
          setQuoteError("Unable to refresh the quote for the selected pickup and drop locations.")
        }
      } finally {
        if (active) {
          setQuoteLoading(false)
        }
      }
    }, 250)

    return () => {
      active = false
      window.clearTimeout(timer)
    }
  }, [dropMode, dropSavedAddressId, dropoffForm, pickupAddressId, savedDropAddress])

  async function loadAddresses() {
    setAddressesLoading(true)
    try {
      const items = await fetchTenantAddressesService()
      const normalized = sortAddresses(items)
      setAddresses(normalized)
      if (normalized.length > 0) {
        setPickupAddressId((current) => current || normalized[0].id)
      }
    } catch (error) {
      console.error("Failed to load addresses", error)
      toast.error("Failed to load saved addresses")
    } finally {
      setAddressesLoading(false)
    }
  }

  async function handleCreateOrder() {
    const activeDrop = getActiveDropoff(dropMode, savedDropAddress, dropoffForm)
    const validationErrors = validateOrderForm({ pickupAddressId, quote, activeDrop, shipmentForm })
    if (validationErrors.length > 0) {
      toast.error(validationErrors[0])
      return
    }

    const payload: TenantCreateOrderInput = {
      quoteSessionId: quote!.quoteSessionId,
      orderReference: shipmentForm.orderReference.trim(),
      paymentType: shipmentForm.paymentType,
      pickupAddressId,
      dropoff: {
        addressId: dropMode === "saved" ? savedDropAddress!.id : undefined,
        label: activeDrop!.label || undefined,
        line1: activeDrop!.line1,
        line2: activeDrop!.line2 || undefined,
        city: activeDrop!.city,
        state: activeDrop!.state,
        country: activeDrop!.country,
        pincode: activeDrop!.pincode,
        locality: activeDrop!.locality || undefined,
        latitude: activeDrop!.latitude,
        longitude: activeDrop!.longitude,
        contactName: activeDrop!.contactName,
        contactPhone: activeDrop!.contactPhone,
        businessName: activeDrop!.businessName || undefined,
        notes: activeDrop!.notes || undefined,
      },
      packageInfo: {
        totalValue: shipmentForm.declaredValue.trim() ? Number(shipmentForm.declaredValue) : null,
        totalWeightGrams: Math.round(Number(shipmentForm.weightKg) * 1000),
        size: shipmentForm.packageSize,
        description: shipmentForm.packageDescription.trim(),
      },
      deliveryInstructions: shipmentForm.deliveryInstructions.trim() || null,
    }

    setSubmitting(true)
    try {
      await createTenantOrderService(payload)
      toast.success("Shipment created successfully")
      router.push("/orders")
    } catch (error) {
      console.error("Failed to create shipment", error)
      toast.error("Failed to create shipment")
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="mx-auto flex w-full max-w-7xl flex-col gap-6 px-4 py-6 sm:px-6 lg:px-8">
      <section>
        <Card className="overflow-hidden border-slate-200 bg-[radial-gradient(circle_at_top_left,_rgba(14,165,233,0.14),_transparent_26%),radial-gradient(circle_at_bottom_right,_rgba(34,197,94,0.14),_transparent_22%),linear-gradient(135deg,#ffffff,#f8fafc)] shadow-sm">
          <CardContent className="flex flex-col gap-6 px-6 py-8 sm:px-8">
            <div className="flex flex-wrap items-center justify-between gap-4">
              <Button variant="outline" className="rounded-full border-slate-300 bg-white/80" onClick={() => router.back()}>
                <ArrowLeft className="h-4 w-4" />
                Back
              </Button>
              <div className="rounded-full border border-emerald-200 bg-emerald-50 px-3 py-1 text-xs font-medium text-emerald-700">
                Dashboard booking flow
              </div>
            </div>

            <div className="grid gap-6 lg:grid-cols-[1.2fr_0.8fr] lg:items-end">
              <div className="space-y-3">
                <h1 className="text-3xl font-semibold tracking-tight text-slate-950">Create new shipment</h1>
                <p className="max-w-3xl text-sm leading-6 text-slate-600">
                  Pick a saved pickup address, add a verified drop location with live OpenStreetMap suggestions, and
                  SwiftTrack will refresh the quote automatically.
                </p>
              </div>
              <div className="grid gap-3 rounded-3xl border border-slate-200 bg-white/80 p-4 sm:grid-cols-3">
                <QuickStat icon={MapPin} title="Pickup" value={pickupAddress ? pickupAddress.label || pickupAddress.city : "Required"} />
                <QuickStat
                  icon={Truck}
                  title="Drop"
                  value={getActiveDropoff(dropMode, savedDropAddress, dropoffForm)?.city || "Required"}
                />
                <QuickStat icon={Package} title="Quote" value={quote ? `${quote.currency || "INR"} ${quote.price.toFixed(2)}` : "Pending"} />
              </div>
            </div>
          </CardContent>
        </Card>
      </section>

      <section className="grid gap-6 xl:grid-cols-[1.3fr_0.7fr]">
        <div className="space-y-6">
          <Card className="border-slate-200 bg-white shadow-sm">
            <CardHeader className="border-b border-slate-100">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <CardTitle className="text-slate-950">Pickup address</CardTitle>
                  <CardDescription>Address management is now in a separate page so order creation stays focused.</CardDescription>
                </div>
                <Button asChild className="rounded-full bg-slate-950 text-white hover:bg-slate-800">
                  <Link href="/tenant/addresses">Manage addresses</Link>
                </Button>
              </div>
            </CardHeader>
            <CardContent className="space-y-4 pt-6">
              {addressesLoading ? (
                <LoadingState label="Loading saved addresses" />
              ) : addresses.length === 0 ? (
                <EmptyState
                  title="No saved pickup address yet"
                  description="Add your first pickup address to unlock quote calculation and order creation."
                  actionLabel="Open address book"
                  href="/tenant/addresses"
                />
              ) : (
                <div className="grid gap-4">
                  {addresses.map((address) => (
                    <AddressSelectionCard
                      key={address.id}
                      address={address}
                      selected={pickupAddressId === address.id}
                      onSelect={() => setPickupAddressId(address.id)}
                    />
                  ))}
                </div>
              )}
            </CardContent>
          </Card>

          <Card className="border-slate-200 bg-white shadow-sm">
            <CardHeader className="border-b border-slate-100">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <CardTitle className="text-slate-950">Drop location</CardTitle>
                  <CardDescription>
                    Use a saved address or add a one-time delivery location with live place suggestions.
                  </CardDescription>
                </div>
                <Button asChild variant="outline" className="rounded-full border-slate-300 bg-white">
                  <Link href="/tenant/addresses">Manage addresses</Link>
                </Button>
              </div>
            </CardHeader>
            <CardContent className="space-y-6 pt-6">
              <div className="inline-flex rounded-full border border-slate-200 bg-slate-50 p-1">
                <button type="button" className={toggleClassName(dropMode === "one-time")} onClick={() => setDropMode("one-time")}>
                  One-time address
                </button>
                <button type="button" className={toggleClassName(dropMode === "saved")} onClick={() => setDropMode("saved")}>
                  Saved address
                </button>
              </div>

              {dropMode === "saved" ? (
                addresses.length === 0 ? (
                  <EmptyState
                    title="No saved addresses available"
                    description="Add an address first, then you can use it for dropoff as well."
                    actionLabel="Open address book"
                    href="/tenant/addresses"
                  />
                ) : (
                  <div className="grid gap-4">
                    {addresses.map((address) => (
                      <AddressSelectionCard
                        key={`drop-${address.id}`}
                        address={address}
                        selected={dropSavedAddressId === address.id}
                        onSelect={() => setDropSavedAddressId(address.id)}
                      />
                    ))}
                  </div>
                )
              ) : (
                <OneTimeDropForm form={dropoffForm} onChange={setDropoffForm} />
              )}
            </CardContent>
          </Card>

          <Card className="border-slate-200 bg-white shadow-sm">
            <CardHeader className="border-b border-slate-100">
              <CardTitle className="text-slate-950">Shipment details</CardTitle>
              <CardDescription>Validate package and payment details before creating the shipment.</CardDescription>
            </CardHeader>
            <CardContent className="grid gap-6 pt-6 md:grid-cols-2">
              <Field
                label="Order reference"
                value={shipmentForm.orderReference}
                onChange={(value) => setShipmentForm((current) => ({ ...current, orderReference: value }))}
                placeholder="SWIFT-ORD-1001"
              />
              <SelectField
                label="Payment type"
                value={shipmentForm.paymentType}
                onChange={(value) => setShipmentForm((current) => ({ ...current, paymentType: value as ShipmentFormState["paymentType"] }))}
                options={[
                  { label: "Prepaid", value: "PREPAID" },
                  { label: "Cash on delivery", value: "COD" },
                ]}
              />
              <Field
                label="Package weight (kg)"
                type="number"
                min="0"
                step="0.1"
                value={shipmentForm.weightKg}
                onChange={(value) => setShipmentForm((current) => ({ ...current, weightKg: value }))}
                placeholder="2.5"
              />
              <SelectField
                label="Package size"
                value={shipmentForm.packageSize}
                onChange={(value) => setShipmentForm((current) => ({ ...current, packageSize: value as ShipmentFormState["packageSize"] }))}
                options={[
                  { label: "Small", value: "SMALL" },
                  { label: "Medium", value: "MEDIUM" },
                  { label: "Large", value: "LARGE" },
                  { label: "XL", value: "XL" },
                ]}
              />
              <Field
                label="Declared value (INR)"
                type="number"
                min="0"
                step="1"
                value={shipmentForm.declaredValue}
                onChange={(value) => setShipmentForm((current) => ({ ...current, declaredValue: value }))}
                placeholder="2500"
              />
              <Field
                label="Package description"
                value={shipmentForm.packageDescription}
                onChange={(value) => setShipmentForm((current) => ({ ...current, packageDescription: value }))}
                placeholder="Documents, spare parts, electronics..."
              />
              <div className="md:col-span-2">
                <Label className="mb-2 block text-sm text-slate-700">Delivery instructions</Label>
                <Textarea
                  className="min-h-28 border-slate-200 bg-slate-50"
                  value={shipmentForm.deliveryInstructions}
                  onChange={(event) => setShipmentForm((current) => ({ ...current, deliveryInstructions: event.target.value }))}
                  placeholder="Gate number, landmark, preferred arrival guidance..."
                />
              </div>
            </CardContent>
          </Card>
        </div>

        <div className="space-y-6">
          <Card className="border-slate-200 bg-white shadow-sm xl:sticky xl:top-6">
            <CardHeader className="border-b border-slate-100">
              <CardTitle className="text-slate-950">Dispatch summary</CardTitle>
              <CardDescription>The quote refreshes automatically once pickup and drop are valid.</CardDescription>
            </CardHeader>
            <CardContent className="space-y-6 pt-6">
              <SummaryBlock title="Pickup" value={pickupAddress ? formatAddressBlock(pickupAddress) : "Select a pickup address"} />
              <SummaryBlock
                title="Dropoff"
                value={
                  getActiveDropoff(dropMode, savedDropAddress, dropoffForm)
                    ? formatAddressBlock(getActiveDropoff(dropMode, savedDropAddress, dropoffForm)!)
                    : "Add a valid drop location"
                }
              />
              <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                <div className="mb-2 flex items-center justify-between">
                  <p className="text-sm font-medium text-slate-700">Live quote</p>
                  {quoteLoading ? <RefreshCw className="h-4 w-4 animate-spin text-slate-400" /> : null}
                </div>
                {quote ? (
                  <div className="space-y-2">
                    <div className="text-3xl font-semibold tracking-tight text-slate-950">
                      {quote.currency || "INR"} {quote.price.toFixed(2)}
                    </div>
                    <div className="text-sm text-slate-600">
                      {quote.providerCode || "SwiftTrack"} via {quote.selectedType || "default routing"}
                    </div>
                    <div className="text-xs text-slate-500">Quote session: {quote.quoteSessionId}</div>
                  </div>
                ) : (
                  <p className="text-sm leading-6 text-slate-600">
                    {quoteError || "Select pickup and drop locations to calculate the latest quote."}
                  </p>
                )}
              </div>
              <Button className="w-full rounded-full bg-slate-950 text-white hover:bg-slate-800" disabled={submitting || quoteLoading || !quote} onClick={handleCreateOrder}>
                {submitting ? <Loader2 className="h-4 w-4 animate-spin" /> : <CheckCircle2 className="h-4 w-4" />}
                Create shipment
              </Button>
            </CardContent>
          </Card>
        </div>
      </section>
    </div>
  )
}

function OneTimeDropForm({
  form,
  onChange,
}: {
  form: AddressFormState
  onChange: React.Dispatch<React.SetStateAction<AddressFormState>>
}) {
  function updateField(field: keyof AddressFormState, value: string) {
    onChange((current) => ({ ...current, [field]: value }))
  }

  function handlePlaceSelect(place: TenantPlaceSuggestion) {
    onChange((current) => ({
      ...current,
      line1: getPrimaryLine(place),
      city: place.city || current.city,
      state: place.state || current.state,
      country: place.country || current.country || "India",
      pincode: place.postalCode || current.pincode,
      locality: place.locality || current.locality,
      latitude: String(place.latitude),
      longitude: String(place.longitude),
    }))
  }

  return (
    <div className="space-y-6">
      <div className="space-y-1">
        <h3 className="text-base font-semibold text-slate-950">One-time drop details</h3>
        <p className="text-sm text-slate-600">Search a place live, pick the right result, then complete the receiver details.</p>
      </div>

      <PlaceAutocompleteInput onSelect={handlePlaceSelect} />

      <div className="grid gap-4 md:grid-cols-2">
        <Field label="Label" value={form.label} onChange={(value) => updateField("label", value)} placeholder="Customer, branch, warehouse..." />
        <Field label="Business name" value={form.businessName} onChange={(value) => updateField("businessName", value)} placeholder="Receiver business name" />
        <Field label="Address line 1" value={form.line1} onChange={(value) => updateField("line1", value)} placeholder="Street, building, landmark" />
        <Field label="Address line 2" value={form.line2} onChange={(value) => updateField("line2", value)} placeholder="Floor, suite, unit" />
        <Field label="Locality" value={form.locality} onChange={(value) => updateField("locality", value)} placeholder="Locality / area" />
        <Field label="City" value={form.city} onChange={(value) => updateField("city", value)} placeholder="Bengaluru" />
        <Field label="State" value={form.state} onChange={(value) => updateField("state", value)} placeholder="Karnataka" />
        <Field label="Country" value={form.country} onChange={(value) => updateField("country", value)} placeholder="India" />
        <Field label="Pincode" value={form.pincode} onChange={(value) => updateField("pincode", value)} placeholder="560001" />
        <Field label="Contact person" value={form.contactName} onChange={(value) => updateField("contactName", value)} placeholder="Receiver name" />
        <Field label="Contact phone" value={form.contactPhone} onChange={(value) => updateField("contactPhone", value)} placeholder="9876543210" />
        <div className="grid gap-4 sm:grid-cols-2">
          <Field label="Latitude" value={form.latitude} onChange={(value) => updateField("latitude", value)} placeholder="12.9716" />
          <Field label="Longitude" value={form.longitude} onChange={(value) => updateField("longitude", value)} placeholder="77.5946" />
        </div>
      </div>

      <div>
        <Label className="mb-2 block text-sm text-slate-700">Notes</Label>
        <Textarea className="min-h-24 border-slate-200 bg-slate-50" value={form.notes} onChange={(event) => updateField("notes", event.target.value)} placeholder="Landmark, gate info, receiving timings..." />
      </div>
    </div>
  )
}

function PlaceAutocompleteInput({ onSelect }: { onSelect: (place: TenantPlaceSuggestion) => void }) {
  const [query, setQuery] = useState("")
  const [results, setResults] = useState<TenantPlaceSuggestion[]>([])
  const [loading, setLoading] = useState(false)
  const [open, setOpen] = useState(false)
  const [message, setMessage] = useState("Start typing to search places in India.")

  useEffect(() => {
    if (query.trim().length < 3) {
      setResults([])
      setOpen(false)
      setMessage("Type at least 3 characters to search places.")
      return
    }

    let active = true
    const timer = window.setTimeout(async () => {
      setLoading(true)
      setOpen(true)
      setMessage("Searching places...")
      try {
        const suggestions = await fetchPlaceSuggestionsService(query.trim(), 5)
        if (!active) {
          return
        }
        setResults(suggestions)
        setMessage(suggestions.length > 0 ? "" : "No matching places found.")
      } catch (error) {
        console.error("Failed to fetch place suggestions", error)
        if (active) {
          setResults([])
          setMessage("Place search failed. Check MapService availability.")
        }
      } finally {
        if (active) {
          setLoading(false)
        }
      }
    }, 200)

    return () => {
      active = false
      window.clearTimeout(timer)
    }
  }, [query])

  return (
    <div className="relative">
      <Label className="mb-2 block text-sm text-slate-700">Search place</Label>
      <div className="relative">
        <MapPin className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
        <Input
          className="border-slate-200 bg-slate-50 pl-10"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          onFocus={() => setOpen(true)}
          placeholder="Search locality, building, landmark, or pincode"
        />
        {loading ? <Loader2 className="absolute right-3 top-1/2 h-4 w-4 -translate-y-1/2 animate-spin text-slate-400" /> : null}
      </div>
      {open ? (
        <div className="absolute z-20 mt-2 max-h-72 w-full overflow-auto rounded-2xl border border-slate-200 bg-white p-2 shadow-xl">
          {results.length > 0 ? (
            results.map((result, index) => (
              <button
                key={`${result.placeId ?? "place"}-${result.latitude}-${result.longitude}-${index}`}
                type="button"
                className="flex w-full flex-col rounded-xl px-3 py-3 text-left transition hover:bg-slate-50"
                onMouseDown={(event) => event.preventDefault()}
                onClick={() => {
                  onSelect(result)
                  setQuery(result.formattedAddress || result.displayName || "")
                  setOpen(false)
                }}
              >
                <span className="text-sm font-medium text-slate-900">{getPrimaryLine(result)}</span>
                <span className="mt-1 text-xs leading-5 text-slate-500">{result.formattedAddress || result.displayName}</span>
              </button>
            ))
          ) : (
            <div className="px-3 py-4 text-sm text-slate-500">{message}</div>
          )}
        </div>
      ) : null}
    </div>
  )
}

function AddressSelectionCard({
  address,
  selected,
  onSelect,
}: {
  address: TenantSavedAddress
  selected: boolean
  onSelect: () => void
}) {
  return (
    <div
      role="button"
      tabIndex={0}
      onClick={onSelect}
      onKeyDown={(event) => {
        if (event.key === "Enter" || event.key === " ") {
          event.preventDefault()
          onSelect()
        }
      }}
      className={`w-full rounded-3xl border p-5 text-left transition ${
        selected
          ? "border-slate-950 bg-slate-950 text-white shadow-lg shadow-slate-950/10"
          : "border-slate-200 bg-white text-slate-900 hover:border-slate-300 hover:bg-slate-50"
      }`}
    >
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div className="space-y-2">
          <div className="flex flex-wrap items-center gap-2">
            <span className="text-sm font-semibold">{address.label || address.businessName || address.city}</span>
            {address.isDefault ? (
              <span className={`inline-flex items-center gap-1 rounded-full px-2.5 py-1 text-xs ${selected ? "bg-white/15 text-white" : "bg-amber-100 text-amber-700"}`}>
                <Star className="h-3 w-3" />
                Default
              </span>
            ) : null}
          </div>
          <p className={`text-sm leading-6 ${selected ? "text-slate-200" : "text-slate-600"}`}>{formatAddressBlock(address)}</p>
          <div className={`flex items-center gap-2 text-xs ${selected ? "text-slate-300" : "text-slate-500"}`}>
            <Phone className="h-3 w-3" />
            {address.contactName} · {address.contactPhone}
          </div>
        </div>
        {selected ? <CheckCircle2 className="h-5 w-5 shrink-0 text-emerald-300" /> : null}
      </div>
    </div>
  )
}

function Field({
  label,
  value,
  onChange,
  placeholder,
  type = "text",
  ...props
}: {
  label: string
  value: string
  onChange: (value: string) => void
  placeholder?: string
  type?: React.HTMLInputTypeAttribute
} & Omit<React.ComponentProps<typeof Input>, "value" | "onChange" | "type">) {
  return (
    <div>
      <Label className="mb-2 block text-sm text-slate-700">{label}</Label>
      <Input {...props} type={type} className="border-slate-200 bg-slate-50" value={value} onChange={(event) => onChange(event.target.value)} placeholder={placeholder} />
    </div>
  )
}

function SelectField({
  label,
  value,
  onChange,
  options,
}: {
  label: string
  value: string
  onChange: (value: string) => void
  options: Array<{ label: string; value: string }>
}) {
  return (
    <div>
      <Label className="mb-2 block text-sm text-slate-700">{label}</Label>
      <select className="h-10 w-full rounded-md border border-slate-200 bg-slate-50 px-3 text-sm text-slate-900 outline-none transition focus:border-slate-400" value={value} onChange={(event) => onChange(event.target.value)}>
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </div>
  )
}

function QuickStat({ icon: Icon, title, value }: { icon: typeof MapPin; title: string; value: string }) {
  return (
    <div className="rounded-2xl border border-slate-200 bg-white/80 p-3">
      <div className="flex items-center gap-2 text-xs font-medium uppercase tracking-wide text-slate-500">
        <Icon className="h-3.5 w-3.5" />
        {title}
      </div>
      <div className="mt-2 text-sm font-semibold text-slate-900">{value}</div>
    </div>
  )
}

function SummaryBlock({ title, value }: { title: string; value: string }) {
  return (
    <div>
      <p className="mb-1 text-sm font-medium text-slate-700">{title}</p>
      <p className="text-sm leading-6 text-slate-600">{value}</p>
    </div>
  )
}

function LoadingState({ label }: { label: string }) {
  return (
    <div className="flex items-center gap-3 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-6 text-sm text-slate-600">
      <Loader2 className="h-4 w-4 animate-spin" />
      {label}
    </div>
  )
}

function EmptyState({
  title,
  description,
  actionLabel,
  href,
}: {
  title: string
  description: string
  actionLabel: string
  href: string
}) {
  return (
    <div className="rounded-3xl border border-dashed border-slate-300 bg-slate-50 px-6 py-8 text-center">
      <h3 className="text-base font-semibold text-slate-900">{title}</h3>
      <p className="mt-2 text-sm leading-6 text-slate-600">{description}</p>
      <Button asChild className="mt-4 rounded-full bg-slate-950 text-white hover:bg-slate-800">
        <Link href={href}>{actionLabel}</Link>
      </Button>
    </div>
  )
}

function validateOrderForm({
  pickupAddressId,
  quote,
  activeDrop,
  shipmentForm,
}: {
  pickupAddressId: string
  quote: TenantOrderQuote | null
  activeDrop: ReturnType<typeof getActiveDropoff>
  shipmentForm: ShipmentFormState
}) {
  const errors: string[] = []

  if (!pickupAddressId) errors.push("Select a pickup address")
  if (!activeDrop) errors.push("Add a valid drop location")
  if (!quote?.quoteSessionId) errors.push("Wait for a valid quote before creating the shipment")
  if (!shipmentForm.orderReference.trim()) errors.push("Order reference is required")
  if (!shipmentForm.packageDescription.trim()) errors.push("Package description is required")

  const weightKg = Number(shipmentForm.weightKg)
  if (Number.isNaN(weightKg) || weightKg <= 0) errors.push("Package weight must be greater than zero")

  if (shipmentForm.declaredValue.trim()) {
    const declaredValue = Number(shipmentForm.declaredValue)
    if (Number.isNaN(declaredValue) || declaredValue < 0) {
      errors.push("Declared value must be a valid positive amount")
    }
  }

  if (activeDrop && !/^\d{10,15}$/.test(activeDrop.contactPhone.replace(/\D/g, ""))) {
    errors.push("Drop contact phone must be valid")
  }

  return errors
}

function getActiveDropoff(dropMode: DropMode, savedDropAddress: TenantSavedAddress | null, dropoffForm: AddressFormState) {
  if (dropMode === "saved") {
    if (!savedDropAddress || savedDropAddress.latitude == null || savedDropAddress.longitude == null) {
      return null
    }

    return {
      addressId: savedDropAddress.id,
      label: savedDropAddress.label,
      line1: savedDropAddress.line1,
      line2: savedDropAddress.line2,
      city: savedDropAddress.city,
      state: savedDropAddress.state,
      country: savedDropAddress.country,
      pincode: savedDropAddress.pincode,
      locality: savedDropAddress.locality,
      latitude: savedDropAddress.latitude,
      longitude: savedDropAddress.longitude,
      contactName: savedDropAddress.contactName,
      contactPhone: savedDropAddress.contactPhone,
      businessName: savedDropAddress.businessName,
      notes: savedDropAddress.notes,
    }
  }

  const latitude = Number(dropoffForm.latitude)
  const longitude = Number(dropoffForm.longitude)
  if (
    !dropoffForm.line1.trim() ||
    !dropoffForm.city.trim() ||
    !dropoffForm.state.trim() ||
    !dropoffForm.country.trim() ||
    !dropoffForm.pincode.trim() ||
    !dropoffForm.contactName.trim() ||
    !dropoffForm.contactPhone.trim() ||
    Number.isNaN(latitude) ||
    Number.isNaN(longitude)
  ) {
    return null
  }

  return {
    label: dropoffForm.label.trim() || null,
    line1: dropoffForm.line1.trim(),
    line2: dropoffForm.line2.trim() || null,
    city: dropoffForm.city.trim(),
    state: dropoffForm.state.trim(),
    country: dropoffForm.country.trim(),
    pincode: dropoffForm.pincode.trim(),
    locality: dropoffForm.locality.trim() || null,
    latitude,
    longitude,
    contactName: dropoffForm.contactName.trim(),
    contactPhone: dropoffForm.contactPhone.trim(),
    businessName: dropoffForm.businessName.trim() || null,
    notes: dropoffForm.notes.trim() || null,
  }
}

function formatAddressBlock(address: {
  line1: string
  line2?: string | null
  locality?: string | null
  city: string
  state: string
  pincode: string
  country: string
}) {
  return [address.line1, address.line2, address.locality, `${address.city}, ${address.state} ${address.pincode}`, address.country]
    .filter(Boolean)
    .join(", ")
}

function getPrimaryLine(place: TenantPlaceSuggestion) {
  const source = place.formattedAddress || place.displayName || ""
  const firstSegment = source.split(",")[0]?.trim()
  return firstSegment || source
}

function sortAddresses(addresses: TenantSavedAddress[]) {
  return [...addresses].sort((left, right) => {
    if (left.isDefault === right.isDefault) {
      return 0
    }
    return left.isDefault ? -1 : 1
  })
}

function toggleClassName(active: boolean) {
  return `rounded-full px-4 py-2 text-sm transition ${active ? "bg-slate-950 text-white shadow-sm" : "text-slate-600 hover:text-slate-900"}`
}
