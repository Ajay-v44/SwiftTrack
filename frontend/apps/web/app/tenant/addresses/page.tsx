"use client"

import type React from "react"
import { useEffect, useState } from "react"
import Link from "next/link"
import { ArrowLeft, CheckCircle2, Loader2, MapPin, PencilLine, Phone, Plus, Star } from "lucide-react"
import { toast } from "sonner"
import {
  createTenantAddressService,
  fetchPlaceSuggestionsService,
  fetchTenantAddressesService,
  setTenantDefaultAddressService,
  updateTenantAddressService,
} from "@swifttrack/services"
import type { TenantPlaceSuggestion, TenantSavedAddress, TenantSavedAddressInput } from "@swifttrack/types"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"

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
  isDefault: boolean
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
  isDefault: false,
}

export default function TenantAddressesPage() {
  const [addresses, setAddresses] = useState<TenantSavedAddress[]>([])
  const [loading, setLoading] = useState(true)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [dialogLoading, setDialogLoading] = useState(false)
  const [editingAddress, setEditingAddress] = useState<TenantSavedAddress | null>(null)
  const [form, setForm] = useState<AddressFormState>(EMPTY_ADDRESS_FORM)

  useEffect(() => {
    void loadAddresses()
  }, [])

  async function loadAddresses() {
    setLoading(true)
    try {
      const items = await fetchTenantAddressesService()
      setAddresses(sortAddresses(items))
    } catch (error) {
      console.error("Failed to load addresses", error)
      toast.error("Failed to load saved addresses")
    } finally {
      setLoading(false)
    }
  }

  function openCreateDialog() {
    setEditingAddress(null)
    setForm(EMPTY_ADDRESS_FORM)
    setDialogOpen(true)
  }

  function openEditDialog(address: TenantSavedAddress) {
    setEditingAddress(address)
    setForm(toAddressFormState(address))
    setDialogOpen(true)
  }

  async function handleSubmit() {
    const validationErrors = validateAddressForm(form)
    if (validationErrors.length > 0) {
      toast.error(validationErrors[0])
      return
    }

    setDialogLoading(true)
    try {
      const payload = toAddressPayload(form)
      if (editingAddress) {
        await updateTenantAddressService(editingAddress.id, payload)
        toast.success("Address updated")
      } else {
        await createTenantAddressService(payload)
        toast.success("Address added")
      }

      await loadAddresses()
      setDialogOpen(false)
    } catch (error) {
      console.error("Failed to save address", error)
      toast.error("Failed to save address")
    } finally {
      setDialogLoading(false)
    }
  }

  async function handleSetDefault(addressId: string) {
    try {
      await setTenantDefaultAddressService(addressId)
      await loadAddresses()
      toast.success("Default address updated")
    } catch (error) {
      console.error("Failed to update default address", error)
      toast.error("Failed to update default address")
    }
  }

  return (
    <div className="mx-auto flex w-full max-w-7xl flex-col gap-6 px-4 py-6 sm:px-6 lg:px-8">
      <Card className="overflow-hidden border-slate-200 bg-[radial-gradient(circle_at_top_left,_rgba(251,191,36,0.12),_transparent_24%),radial-gradient(circle_at_bottom_right,_rgba(14,165,233,0.12),_transparent_22%),linear-gradient(135deg,#ffffff,#f8fafc)] shadow-sm">
        <CardContent className="flex flex-col gap-6 px-6 py-8 sm:px-8">
          <div className="flex flex-wrap items-center justify-between gap-4">
            <Button asChild variant="outline" className="rounded-full border-slate-300 bg-white/80">
              <Link href="/tenant/orders/create">
                <ArrowLeft className="h-4 w-4" />
                Back to create shipment
              </Link>
            </Button>
            <Button className="rounded-full bg-slate-950 text-white hover:bg-slate-800" onClick={openCreateDialog}>
              <Plus className="h-4 w-4" />
              Add address
            </Button>
          </div>

          <div className="space-y-3">
            <h1 className="text-3xl font-semibold tracking-tight text-slate-950">Saved addresses</h1>
            <p className="max-w-3xl text-sm leading-6 text-slate-600">
              Manage pickup and reusable drop addresses here. Search works live against OpenStreetMap and is biased to India first.
            </p>
          </div>
        </CardContent>
      </Card>

      <Card className="border-slate-200 bg-white shadow-sm">
        <CardHeader className="border-b border-slate-100">
          <CardTitle className="text-slate-950">Address book</CardTitle>
          <CardDescription>Default address appears first and is used as the preferred pickup in the shipment flow.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4 pt-6">
          {loading ? (
            <LoadingState label="Loading saved addresses" />
          ) : addresses.length === 0 ? (
            <div className="rounded-3xl border border-dashed border-slate-300 bg-slate-50 px-6 py-10 text-center">
              <h3 className="text-base font-semibold text-slate-900">No saved addresses yet</h3>
              <p className="mt-2 text-sm leading-6 text-slate-600">Add your first address to enable dashboard shipment creation.</p>
              <Button className="mt-4 rounded-full bg-slate-950 text-white hover:bg-slate-800" onClick={openCreateDialog}>
                <Plus className="h-4 w-4" />
                Add address
              </Button>
            </div>
          ) : (
            <div className="grid gap-4">
              {addresses.map((address) => (
                <AddressManagerCard
                  key={address.id}
                  address={address}
                  onEdit={() => openEditDialog(address)}
                  onMakeDefault={() => handleSetDefault(address.id)}
                />
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="flex max-h-[92vh] w-[calc(100%-1.5rem)] max-w-3xl flex-col overflow-hidden border-slate-200 bg-white p-0">
          <DialogHeader className="shrink-0 border-b border-slate-100 px-5 py-4 sm:px-6">
            <DialogTitle>{editingAddress ? "Edit saved address" : "Add saved address"}</DialogTitle>
            <DialogDescription>
              Search a place live, verify the full address, then save the contact details for pickup or drop reuse.
            </DialogDescription>
          </DialogHeader>

          <div className="min-h-0 flex-1 overflow-y-auto px-5 py-5 sm:px-6">
            <AddressFormSection form={form} onChange={setForm} />
          </div>

          <DialogFooter className="shrink-0 border-t border-slate-100 bg-white px-5 py-4 sm:px-6">
            <Button variant="outline" className="w-full sm:w-auto" onClick={() => setDialogOpen(false)}>
              Cancel
            </Button>
            <Button
              className="w-full bg-slate-950 text-white hover:bg-slate-800 sm:w-auto"
              disabled={dialogLoading}
              onClick={handleSubmit}
            >
              {dialogLoading ? <Loader2 className="h-4 w-4 animate-spin" /> : null}
              {editingAddress ? "Save changes" : "Add address"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}

function AddressFormSection({
  form,
  onChange,
}: {
  form: AddressFormState
  onChange: React.Dispatch<React.SetStateAction<AddressFormState>>
}) {
  function updateField(field: keyof AddressFormState, value: string | boolean) {
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
      <PlaceAutocompleteInput onSelect={handlePlaceSelect} />

      <div className="grid gap-4 md:grid-cols-2">
        <Field label="Label" value={form.label} onChange={(value) => updateField("label", value)} placeholder="Warehouse, HQ, Storefront..." />
        <Field label="Business name" value={form.businessName} onChange={(value) => updateField("businessName", value)} placeholder="SwiftTrack Bengaluru Hub" />
        <Field label="Address line 1" value={form.line1} onChange={(value) => updateField("line1", value)} placeholder="Street, building, landmark" />
        <Field label="Address line 2" value={form.line2} onChange={(value) => updateField("line2", value)} placeholder="Floor, suite, unit" />
        <Field label="Locality" value={form.locality} onChange={(value) => updateField("locality", value)} placeholder="Locality / area" />
        <Field label="City" value={form.city} onChange={(value) => updateField("city", value)} placeholder="Bengaluru" />
        <Field label="State" value={form.state} onChange={(value) => updateField("state", value)} placeholder="Karnataka" />
        <Field label="Country" value={form.country} onChange={(value) => updateField("country", value)} placeholder="India" />
        <Field label="Pincode" value={form.pincode} onChange={(value) => updateField("pincode", value)} placeholder="560001" />
        <Field label="Contact person" value={form.contactName} onChange={(value) => updateField("contactName", value)} placeholder="Operations manager" />
        <Field label="Contact phone" value={form.contactPhone} onChange={(value) => updateField("contactPhone", value)} placeholder="9876543210" />
        <div className="grid gap-4">
          <Field label="Latitude" value={form.latitude} onChange={(value) => updateField("latitude", value)} placeholder="12.9716" />
          <Field label="Longitude" value={form.longitude} onChange={(value) => updateField("longitude", value)} placeholder="77.5946" />
        </div>
      </div>

      <div>
        <Label className="mb-2 block text-sm text-slate-700">Notes</Label>
        <Textarea className="min-h-24 border-slate-200 bg-slate-50" value={form.notes} onChange={(event) => updateField("notes", event.target.value)} placeholder="Entry instructions, loading bay notes, landmark details..." />
      </div>

      <label className="flex items-center gap-3 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-700">
        <input type="checkbox" className="h-4 w-4 rounded border-slate-300" checked={form.isDefault} onChange={(event) => updateField("isDefault", event.target.checked)} />
        Set this as the default pickup address
      </label>
    </div>
  )
}

function PlaceAutocompleteInput({ onSelect }: { onSelect: (place: TenantPlaceSuggestion) => void }) {
  const [query, setQuery] = useState("")
  const [results, setResults] = useState<TenantPlaceSuggestion[]>([])
  const [loading, setLoading] = useState(false)
  const [open, setOpen] = useState(false)
  const [message, setMessage] = useState("Type at least 3 characters to search places.")

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
        <Input className="border-slate-200 bg-slate-50 pl-10" value={query} onChange={(event) => setQuery(event.target.value)} onFocus={() => setOpen(true)} placeholder="Search locality, building, landmark, or pincode" />
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

function AddressManagerCard({
  address,
  onEdit,
  onMakeDefault,
}: {
  address: TenantSavedAddress
  onEdit: () => void
  onMakeDefault: () => void
}) {
  return (
    <div className={`rounded-3xl border p-5 ${address.isDefault ? "border-slate-950 bg-slate-950 text-white" : "border-slate-200 bg-white"}`}>
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div className="space-y-2">
          <div className="flex flex-wrap items-center gap-2">
            <span className="text-sm font-semibold">{address.label || address.businessName || address.city}</span>
            {address.isDefault ? (
              <span className="inline-flex items-center gap-1 rounded-full bg-white/15 px-2.5 py-1 text-xs text-white">
                <Star className="h-3 w-3" />
                Default
              </span>
            ) : null}
          </div>
          <p className={`text-sm leading-6 ${address.isDefault ? "text-slate-200" : "text-slate-600"}`}>{formatAddressBlock(address)}</p>
          <div className={`flex items-center gap-2 text-xs ${address.isDefault ? "text-slate-300" : "text-slate-500"}`}>
            <Phone className="h-3 w-3" />
            {address.contactName} · {address.contactPhone}
          </div>
        </div>
        {address.isDefault ? <CheckCircle2 className="h-5 w-5 shrink-0 text-emerald-300" /> : null}
      </div>
      <div className="mt-4 flex flex-wrap gap-2">
        <Button
          type="button"
          size="sm"
          variant={address.isDefault ? "secondary" : "outline"}
          className={`rounded-full ${address.isDefault ? "bg-white text-slate-950 hover:bg-slate-100" : "border-slate-300 bg-white"}`}
          onClick={onEdit}
        >
          <PencilLine className="h-3.5 w-3.5" />
          Edit
        </Button>
        {!address.isDefault ? (
          <Button type="button" size="sm" variant="outline" className="rounded-full border-slate-300 bg-white" onClick={onMakeDefault}>
            <Star className="h-3.5 w-3.5" />
            Make default
          </Button>
        ) : null}
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

function LoadingState({ label }: { label: string }) {
  return (
    <div className="flex items-center gap-3 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-6 text-sm text-slate-600">
      <Loader2 className="h-4 w-4 animate-spin" />
      {label}
    </div>
  )
}

function toAddressFormState(address: TenantSavedAddress): AddressFormState {
  return {
    label: address.label || "",
    line1: address.line1,
    line2: address.line2 || "",
    city: address.city,
    state: address.state,
    country: address.country,
    pincode: address.pincode,
    locality: address.locality || "",
    latitude: address.latitude != null ? String(address.latitude) : "",
    longitude: address.longitude != null ? String(address.longitude) : "",
    contactName: address.contactName,
    contactPhone: address.contactPhone,
    businessName: address.businessName || "",
    notes: address.notes || "",
    isDefault: address.isDefault,
  }
}

function toAddressPayload(form: AddressFormState): TenantSavedAddressInput {
  return {
    label: form.label.trim() || null,
    line1: form.line1.trim(),
    line2: form.line2.trim() || null,
    city: form.city.trim(),
    state: form.state.trim(),
    country: form.country.trim(),
    pincode: form.pincode.trim(),
    locality: form.locality.trim() || null,
    latitude: Number(form.latitude),
    longitude: Number(form.longitude),
    contactName: form.contactName.trim(),
    contactPhone: form.contactPhone.trim(),
    businessName: form.businessName.trim() || null,
    notes: form.notes.trim() || null,
    isDefault: form.isDefault,
  }
}

function validateAddressForm(form: AddressFormState) {
  const errors: string[] = []

  if (!form.line1.trim()) errors.push("Address line 1 is required")
  if (!form.city.trim()) errors.push("City is required")
  if (!form.state.trim()) errors.push("State is required")
  if (!form.country.trim()) errors.push("Country is required")
  if (!form.pincode.trim()) errors.push("Pincode is required")
  if (!form.contactName.trim()) errors.push("Contact person is required")
  if (!/^\d{10,15}$/.test(form.contactPhone.replace(/\D/g, ""))) errors.push("Enter a valid contact phone number")

  const latitude = Number(form.latitude)
  const longitude = Number(form.longitude)
  if (Number.isNaN(latitude) || latitude < -90 || latitude > 90) errors.push("Latitude must be a valid coordinate")
  if (Number.isNaN(longitude) || longitude < -180 || longitude > 180) errors.push("Longitude must be a valid coordinate")

  return errors
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
