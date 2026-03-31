"use client"

import { useEffect, useRef } from "react"
import { Clock3, MapPin, Navigation, Route, Warehouse } from "lucide-react"
import type {
  TenantOrderCurrentLocationInfo,
  TenantOrderLocationInfo,
} from "@swifttrack/types"

type LeafletMap = {
  remove: () => void
  fitBounds: (bounds: unknown, options?: { padding?: [number, number]; maxZoom?: number }) => void
  setView: (center: [number, number], zoom: number) => void
  invalidateSize: () => void
}

type LeafletLayerGroup = {
  clearLayers: () => void
  addTo: (map: LeafletMap) => LeafletLayerGroup
}

type LeafletStatic = {
  map: (element: HTMLElement, options?: Record<string, unknown>) => LeafletMap
  tileLayer: (url: string, options: Record<string, unknown>) => { addTo: (map: LeafletMap) => void }
  layerGroup: () => LeafletLayerGroup
  marker: (coords: [number, number], options?: Record<string, unknown>) => { addTo: (group: LeafletLayerGroup) => void }
  polyline: (coords: [number, number][], options?: Record<string, unknown>) => { addTo: (group: LeafletLayerGroup) => void }
  divIcon: (options: Record<string, unknown>) => unknown
  latLngBounds: (coords: [number, number][]) => unknown
}

declare global {
  interface Window {
    L?: LeafletStatic
  }
}

const LEAFLET_CSS_ID = "swifttrack-leaflet-css"
const LEAFLET_SCRIPT_ID = "swifttrack-leaflet-script"
const LEAFLET_CSS_URL = "https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
const LEAFLET_SCRIPT_URL = "https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"

function ensureLeafletCss() {
  if (document.getElementById(LEAFLET_CSS_ID)) {
    return
  }

  const link = document.createElement("link")
  link.id = LEAFLET_CSS_ID
  link.rel = "stylesheet"
  link.href = LEAFLET_CSS_URL
  document.head.appendChild(link)
}

function loadLeaflet() {
  return new Promise<LeafletStatic>((resolve, reject) => {
    if (window.L) {
      resolve(window.L)
      return
    }

    const existing = document.getElementById(LEAFLET_SCRIPT_ID) as HTMLScriptElement | null
    if (existing) {
      existing.addEventListener("load", () => {
        if (window.L) {
          resolve(window.L)
        }
      })
      existing.addEventListener("error", () => reject(new Error("Failed to load Leaflet")))
      return
    }

    const script = document.createElement("script")
    script.id = LEAFLET_SCRIPT_ID
    script.src = LEAFLET_SCRIPT_URL
    script.async = true
    script.onload = () => {
      if (window.L) {
        resolve(window.L)
      } else {
        reject(new Error("Leaflet unavailable"))
      }
    }
    script.onerror = () => reject(new Error("Failed to load Leaflet"))
    document.body.appendChild(script)
  })
}

function isFiniteCoordinate(value: number | null | undefined): value is number {
  return typeof value === "number" && Number.isFinite(value)
}

function getCoordinates(location?: TenantOrderLocationInfo | TenantOrderCurrentLocationInfo | null) {
  if (!location || !isFiniteCoordinate(location.latitude) || !isFiniteCoordinate(location.longitude)) {
    return null
  }

  return [location.latitude, location.longitude] as [number, number]
}

function pickupIconSvg() {
  return `
    <svg viewBox="0 0 48 48" aria-hidden="true">
      <path fill="#14b8a6" d="M24 3C16.268 3 10 9.268 10 17c0 9.981 12.238 23.497 13.002 24.324a1.35 1.35 0 0 0 1.996 0C25.762 40.497 38 26.981 38 17 38 9.268 31.732 3 24 3Z"/>
      <circle cx="24" cy="17" r="8.5" fill="white"/>
      <path fill="#0f766e" d="M18.5 15.5h11v7h-11z"/>
      <path fill="#0f766e" d="M20 13h8v2h-8z"/>
    </svg>
  `
}

function dropoffIconSvg() {
  return `
    <svg viewBox="0 0 48 48" aria-hidden="true">
      <path fill="#3b82f6" d="M24 3C16.268 3 10 9.268 10 17c0 9.981 12.238 23.497 13.002 24.324a1.35 1.35 0 0 0 1.996 0C25.762 40.497 38 26.981 38 17 38 9.268 31.732 3 24 3Z"/>
      <circle cx="24" cy="17" r="8.5" fill="white"/>
      <path fill="#1d4ed8" d="M18 18.5 24 13l6 5.5V26a1 1 0 0 1-1 1h-3.5v-4.5h-3V27H19a1 1 0 0 1-1-1Z"/>
    </svg>
  `
}

function driverIconSvg() {
  return `
    <svg viewBox="0 0 56 56" aria-hidden="true">
      <circle cx="28" cy="28" r="26" fill="#f97316" />
      <circle cx="28" cy="28" r="21" fill="white" />
      <circle cx="28" cy="22" r="7" fill="#0f172a" opacity="0.9" />
      <path fill="#0f172a" d="M16 42c2.4-5.9 7.3-8.8 12-8.8s9.6 2.9 12 8.8Z" opacity="0.9" />
      <path fill="#fff7ed" d="M40.5 15.5 45 11l.8 6.8Z"/>
    </svg>
  `
}

function getRouteMode(trackingStatus?: string | null, hasDriver?: boolean) {
  if (!hasDriver) {
    return "pickup_to_drop"
  }

  const normalized = (trackingStatus || "").toUpperCase()

  if (normalized === "ASSIGNED") {
    return "driver_to_pickup"
  }

  if (
    normalized === "PICKED_UP" ||
    normalized === "IN_TRANSIT" ||
    normalized === "OUT_FOR_DELIVERY" ||
    normalized === "DELIVERED"
  ) {
    return "driver_to_drop"
  }

  return "driver_to_pickup"
}

function markerMarkup(kind: "pickup" | "dropoff" | "driver") {
  if (kind === "driver") {
    return `
      <div class="swifttrack-driver-marker">
        <div class="swifttrack-driver-marker__pulse"></div>
        <div class="swifttrack-driver-marker__icon">${driverIconSvg()}</div>
      </div>
    `
  }

  return `
    <div class="swifttrack-pin-marker">
      ${kind === "pickup" ? pickupIconSvg() : dropoffIconSvg()}
    </div>
  `
}

export function TenantOrderMap({
  pickup,
  dropoff,
  driverLocation,
  trackingStatus,
}: {
  pickup?: TenantOrderLocationInfo | null
  dropoff?: TenantOrderLocationInfo | null
  driverLocation?: TenantOrderCurrentLocationInfo | null
  trackingStatus?: string | null
}) {
  const containerRef = useRef<HTMLDivElement | null>(null)
  const mapRef = useRef<LeafletMap | null>(null)
  const layerGroupRef = useRef<LeafletLayerGroup | null>(null)

  useEffect(() => {
    let cancelled = false

    async function setup() {
      if (!containerRef.current) {
        return
      }

      ensureLeafletCss()
      const L = await loadLeaflet()
      if (cancelled || !containerRef.current) {
        return
      }

      if (!mapRef.current) {
        mapRef.current = L.map(containerRef.current, {
          zoomControl: false,
          attributionControl: false,
          scrollWheelZoom: false,
        })

        L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
          attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
        }).addTo(mapRef.current)
        layerGroupRef.current = L.layerGroup().addTo(mapRef.current)
      }

      mapRef.current.invalidateSize()

      const points: [number, number][] = []
      const layers = layerGroupRef.current
      if (!layers || !mapRef.current) {
        return
      }

      layers.clearLayers()

      const pickupCoords = getCoordinates(pickup)
      const dropoffCoords = getCoordinates(dropoff)
      const driverCoords = getCoordinates(driverLocation)
      const routeMode = getRouteMode(trackingStatus, Boolean(driverCoords))
      const focusPoints: [number, number][] = []

      if (pickupCoords) {
        points.push(pickupCoords)
        L.marker(pickupCoords, {
          icon: L.divIcon({
            className: "swifttrack-div-icon",
            html: markerMarkup("pickup"),
            iconSize: [28, 34],
            iconAnchor: [14, 31],
          }),
        }).addTo(layers)
      }

      if (dropoffCoords) {
        points.push(dropoffCoords)
        L.marker(dropoffCoords, {
          icon: L.divIcon({
            className: "swifttrack-div-icon",
            html: markerMarkup("dropoff"),
            iconSize: [28, 34],
            iconAnchor: [14, 31],
          }),
        }).addTo(layers)
      }

      if (driverCoords) {
        points.push(driverCoords)
        L.marker(driverCoords, {
          icon: L.divIcon({
            className: "swifttrack-div-icon",
            html: markerMarkup("driver"),
            iconSize: [40, 40],
            iconAnchor: [20, 20],
          }),
        }).addTo(layers)
      }

      if (!driverCoords && pickupCoords && dropoffCoords) {
        focusPoints.push(pickupCoords, dropoffCoords)
        L.polyline([pickupCoords, dropoffCoords], {
          color: "#ffffff",
          weight: 10,
          opacity: 0.92,
          lineCap: "round",
          lineJoin: "round",
        }).addTo(layers)
        L.polyline([pickupCoords, dropoffCoords], {
          color: "#2563eb",
          weight: 5,
          opacity: 0.9,
          lineCap: "round",
          lineJoin: "round",
        }).addTo(layers)
      }

      if (routeMode === "driver_to_pickup" && driverCoords && pickupCoords) {
        focusPoints.push(driverCoords, pickupCoords)
        L.polyline([driverCoords, pickupCoords], {
          color: "#ffffff",
          weight: 10,
          opacity: 0.92,
          lineCap: "round",
          lineJoin: "round",
        }).addTo(layers)

        L.polyline([driverCoords, pickupCoords], {
          color: "#14b8a6",
          weight: 5,
          opacity: 0.95,
          lineCap: "round",
          lineJoin: "round",
        }).addTo(layers)
      }

      if (routeMode === "driver_to_drop" && driverCoords && dropoffCoords) {
        focusPoints.push(driverCoords, dropoffCoords)
        L.polyline([driverCoords, dropoffCoords], {
          color: "#ffffff",
          weight: 10,
          opacity: 0.92,
          lineCap: "round",
          lineJoin: "round",
        }).addTo(layers)

        L.polyline([driverCoords, dropoffCoords], {
          color: "#2563eb",
          weight: 5,
          opacity: 0.95,
          dashArray: "10 10",
          lineCap: "round",
          lineJoin: "round",
        }).addTo(layers)
      }

      const viewportPoints = focusPoints.length > 1 ? focusPoints : points

      if (viewportPoints.length > 1) {
        mapRef.current.fitBounds(L.latLngBounds(viewportPoints), { padding: [28, 28], maxZoom: 17 })
      } else if (points.length === 1) {
        mapRef.current.setView(points[0], 14)
      } else {
        mapRef.current.setView([20.5937, 78.9629], 4)
      }
    }

    void setup()

    return () => {
      cancelled = true
    }
  }, [driverLocation, dropoff, pickup, trackingStatus])

  useEffect(() => {
    return () => {
      mapRef.current?.remove()
      mapRef.current = null
      layerGroupRef.current = null
    }
  }, [])

  return (
    <div className="overflow-hidden rounded-[28px] border border-slate-200 bg-white shadow-sm">
      <div className="relative">
        <div ref={containerRef} className="h-[390px] w-full" />

        <div className="pointer-events-none absolute inset-x-0 top-0 flex items-start justify-between gap-3 p-4">
          <div className="rounded-2xl border border-white/80 bg-white/92 px-4 py-3 shadow-lg backdrop-blur-md">
            <div className="flex items-center gap-2 text-[11px] uppercase tracking-[0.18em] text-slate-500">
              <Route className="h-3.5 w-3.5 text-slate-700" />
              Live Route
            </div>
            <p className="mt-1 text-sm font-semibold text-slate-950">
              {trackingStatus || driverLocation?.status || "Tracking active"}
            </p>
          </div>

          {driverLocation?.updatedAt ? (
            <div className="rounded-2xl border border-white/80 bg-white/92 px-4 py-3 text-right shadow-lg backdrop-blur-md">
              <div className="flex items-center justify-end gap-2 text-[11px] uppercase tracking-[0.18em] text-slate-500">
                <Clock3 className="h-3.5 w-3.5 text-slate-700" />
                Last Update
              </div>
              <p className="mt-1 text-sm font-semibold text-slate-950">{formatMapTime(driverLocation.updatedAt)}</p>
            </div>
          ) : null}
        </div>

        <div className="pointer-events-none absolute inset-x-0 bottom-0 p-4">
          <div className="flex flex-wrap items-center gap-2 rounded-2xl border border-white/80 bg-white/92 p-3 shadow-lg backdrop-blur-md">
            <LegendPill icon={Warehouse} label="Pickup" tone="teal" />
            <LegendPill icon={Navigation} label="Driver" tone="amber" />
            <LegendPill icon={MapPin} label="Dropoff" tone="blue" />
          </div>
        </div>
      </div>
    </div>
  )
}

function LegendPill({
  icon: Icon,
  label,
  tone,
}: {
  icon: typeof MapPin
  label: string
  tone: "teal" | "amber" | "blue"
}) {
  const toneClass = {
    teal: "bg-teal-50 text-teal-700 ring-teal-100",
    amber: "bg-amber-50 text-amber-700 ring-amber-100",
    blue: "bg-blue-50 text-blue-700 ring-blue-100",
  }

  return (
    <span className={`inline-flex items-center gap-2 rounded-full px-3 py-2 text-xs font-medium ring-1 ${toneClass[tone]}`}>
      <Icon className="h-3.5 w-3.5" />
      {label}
    </span>
  )
}

function formatMapTime(value: string) {
  return new Intl.DateTimeFormat("en-IN", {
    day: "2-digit",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(value))
}
