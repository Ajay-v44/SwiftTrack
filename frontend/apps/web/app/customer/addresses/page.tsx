"use client"

import { useEffect, useState } from "react"
import { toast } from "sonner"
import { Loader2, MapPin, Plus, Star } from "lucide-react"
import {
  createTenantAddressService,
  fetchPlaceSuggestionsService,
  fetchTenantAddressesService,
  setTenantDefaultAddressService,
} from "@swifttrack/services"
import type { TenantPlaceSuggestion, TenantSavedAddress, TenantSavedAddressInput } from "@swifttrack/types"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"

const emptyAddress: TenantSavedAddressInput = {
  label: "",
  line1: "",
  line2: "",
  city: "",
  state: "",
  country: "India",
  pincode: "",
  locality: "",
  latitude: 0,
  longitude: 0,
  contactName: "",
  contactPhone: "",
  businessName: "",
  notes: "",
  isDefault: false,
}

export default function CustomerAddressesPage() {
  const [addresses, setAddresses] = useState<TenantSavedAddress[]>([])
  const [form, setForm] = useState<TenantSavedAddressInput>(emptyAddress)
  const [saving, setSaving] = useState(false)

  async function load() {
    setAddresses(await fetchTenantAddressesService())
  }

  useEffect(() => {
    void load().catch(() => toast.error("Failed to load addresses"))
  }, [])

  async function saveAddress() {
    if (!form.line1 || !form.city || !form.state || !form.pincode || !form.contactName || !form.contactPhone) {
      toast.error("Fill address, city, state, pincode, contact name and phone")
      return
    }
    setSaving(true)
    try {
      await createTenantAddressService(form)
      setForm(emptyAddress)
      await load()
      toast.success("Address saved")
    } catch {
      toast.error("Failed to save address")
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="mx-auto grid max-w-7xl gap-6 px-4 py-6 sm:px-6 lg:grid-cols-[0.9fr_1.1fr] lg:px-8">
      <Card className="border-slate-200 bg-white shadow-sm">
        <CardHeader><CardTitle>Add address</CardTitle></CardHeader>
        <CardContent className="grid gap-3">
          <PlaceAutocompleteInput
            onSelect={(place) =>
              setForm((current) => ({
                ...current,
                line1: getPrimaryLine(place),
                city: place.city || current.city,
                state: place.state || current.state,
                country: place.country || current.country || "India",
                pincode: place.postalCode || current.pincode,
                locality: place.locality || current.locality || "",
                latitude: place.latitude,
                longitude: place.longitude,
              }))
            }
          />
          <AddressInput label="Label" value={form.label || ""} onChange={(value) => setForm({ ...form, label: value })} />
          <AddressInput label="Address line 1" value={form.line1} onChange={(value) => setForm({ ...form, line1: value })} />
          <AddressInput label="Address line 2" value={form.line2 || ""} onChange={(value) => setForm({ ...form, line2: value })} />
          <div className="grid gap-3 sm:grid-cols-2">
            <AddressInput label="Locality" value={form.locality || ""} onChange={(value) => setForm({ ...form, locality: value })} />
            <AddressInput label="City" value={form.city} onChange={(value) => setForm({ ...form, city: value })} />
            <AddressInput label="State" value={form.state} onChange={(value) => setForm({ ...form, state: value })} />
            <AddressInput label="Pincode" value={form.pincode} onChange={(value) => setForm({ ...form, pincode: value })} />
            <AddressInput label="Country" value={form.country} onChange={(value) => setForm({ ...form, country: value })} />
            <AddressInput label="Latitude" type="number" value={String(form.latitude || "")} onChange={(value) => setForm({ ...form, latitude: Number(value) })} />
            <AddressInput label="Longitude" type="number" value={String(form.longitude || "")} onChange={(value) => setForm({ ...form, longitude: Number(value) })} />
          </div>
          <AddressInput label="Contact name" value={form.contactName} onChange={(value) => setForm({ ...form, contactName: value })} />
          <AddressInput label="Contact phone" value={form.contactPhone} onChange={(value) => setForm({ ...form, contactPhone: value })} />
          <label className="flex items-center gap-2 text-sm text-slate-600">
            <input type="checkbox" checked={Boolean(form.isDefault)} onChange={(event) => setForm({ ...form, isDefault: event.target.checked })} />
            Make default pickup
          </label>
          <Button className="rounded-full bg-slate-950 text-white hover:bg-slate-800" disabled={saving} onClick={saveAddress}>
            <Plus className="h-4 w-4" /> Save address
          </Button>
        </CardContent>
      </Card>

      <Card className="border-slate-200 bg-white shadow-sm">
        <CardHeader><CardTitle>Saved addresses</CardTitle></CardHeader>
        <CardContent className="space-y-3">
          {addresses.length === 0 ? <p className="text-sm text-slate-500">No addresses yet.</p> : null}
          {addresses.map((address) => (
            <div key={address.id} className="rounded-2xl border border-slate-200 p-4">
              <div className="flex justify-between gap-3">
                <div>
                  <p className="font-medium">{address.label || address.city}</p>
                  <p className="mt-1 text-sm text-slate-600">{[address.line1, address.line2, address.city, address.state, address.pincode].filter(Boolean).join(", ")}</p>
                </div>
                {address.isDefault ? <span className="text-xs text-emerald-600">Default</span> : null}
              </div>
              {!address.isDefault ? (
                <Button variant="outline" size="sm" className="mt-3 rounded-full bg-white" onClick={() => setTenantDefaultAddressService(address.id).then(load)}>
                  <Star className="h-3.5 w-3.5" /> Make default
                </Button>
              ) : null}
            </div>
          ))}
        </CardContent>
      </Card>
    </div>
  )
}

function PlaceAutocompleteInput({ onSelect }: { onSelect: (place: TenantPlaceSuggestion) => void }) {
  const [query, setQuery] = useState("")
  const [results, setResults] = useState<TenantPlaceSuggestion[]>([])
  const [loading, setLoading] = useState(false)
  const [open, setOpen] = useState(false)
  const [message, setMessage] = useState("Type at least 3 characters to search places.")

  function selectPlace(place: TenantPlaceSuggestion) {
    onSelect(place)
    setQuery(place.formattedAddress || place.displayName || "")
    setOpen(false)
  }

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
      <label className="mb-1 grid gap-1 text-sm text-slate-600">
        Search place
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
      </label>
      {open ? (
        <div className="absolute z-20 mt-1 max-h-72 w-full overflow-auto rounded-2xl border border-slate-200 bg-white p-2 shadow-xl">
          {results.length > 0 ? (
            results.map((result, index) => (
              <button
                key={`${result.placeId ?? "place"}-${result.latitude}-${result.longitude}-${index}`}
                type="button"
                className="flex w-full flex-col rounded-xl px-3 py-3 text-left transition hover:bg-slate-50"
                onPointerDown={(event) => {
                  event.preventDefault()
                  selectPlace(result)
                }}
                onClick={(event) => {
                  if (event.detail === 0) {
                    selectPlace(result)
                  }
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

function AddressInput({ label, value, onChange, type = "text" }: { label: string; value: string; onChange: (value: string) => void; type?: string }) {
  return (
    <label className="grid gap-1 text-sm text-slate-600">
      {label}
      <Input type={type} value={value} onChange={(event) => onChange(event.target.value)} className="border-slate-200 bg-slate-50" />
    </label>
  )
}

function getPrimaryLine(place: TenantPlaceSuggestion) {
  const source = place.formattedAddress || place.displayName || ""
  const firstSegment = source.split(",")[0]?.trim()
  return firstSegment || source
}
