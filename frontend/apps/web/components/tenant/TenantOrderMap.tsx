"use client"

import { useEffect, useRef } from "react"
import { MapPin, Navigation } from "lucide-react"
import type {
  TenantOrderCurrentLocationInfo,
  TenantOrderLocationInfo,
} from "@swifttrack/types"

type LeafletMap = {
  remove: () => void
  fitBounds: (bounds: unknown, options?: { padding?: [number, number] }) => void
  setView: (center: [number, number], zoom: number) => void
}

type LeafletLayerGroup = {
  clearLayers: () => void
  addTo: (map: LeafletMap) => LeafletLayerGroup
}

type LeafletStatic = {
  map: (element: HTMLElement) => LeafletMap
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

function markerMarkup(label: string, tone: string, icon: string) {
  return `
    <div style="display:flex;align-items:center;gap:8px;transform:translate(-6px,-28px);">
      <div style="width:32px;height:32px;border-radius:9999px;background:${tone};color:#fff;display:flex;align-items:center;justify-content:center;font-weight:700;font-size:12px;box-shadow:0 10px 24px rgba(15,23,42,0.18);">
        ${icon}
      </div>
      <div style="padding:6px 10px;border-radius:9999px;background:rgba(15,23,42,0.92);color:#fff;font-size:12px;font-weight:600;white-space:nowrap;">
        ${label}
      </div>
    </div>
  `
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

export function TenantOrderMap({
  pickup,
  dropoff,
  driverLocation,
}: {
  pickup?: TenantOrderLocationInfo | null
  dropoff?: TenantOrderLocationInfo | null
  driverLocation?: TenantOrderCurrentLocationInfo | null
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
        mapRef.current = L.map(containerRef.current)
        L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
          attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
        }).addTo(mapRef.current)
        layerGroupRef.current = L.layerGroup().addTo(mapRef.current)
      }

      const points: [number, number][] = []
      const layers = layerGroupRef.current
      if (!layers || !mapRef.current) {
        return
      }

      layers.clearLayers()

      const pickupCoords = getCoordinates(pickup)
      const dropoffCoords = getCoordinates(dropoff)
      const driverCoords = getCoordinates(driverLocation)

      if (pickupCoords) {
        points.push(pickupCoords)
        L.marker(pickupCoords, {
          icon: L.divIcon({
            className: "",
            html: markerMarkup("Pickup", "#0f766e", "P"),
            iconSize: [120, 42],
            iconAnchor: [16, 32],
          }),
        }).addTo(layers)
      }

      if (dropoffCoords) {
        points.push(dropoffCoords)
        L.marker(dropoffCoords, {
          icon: L.divIcon({
            className: "",
            html: markerMarkup("Drop", "#2563eb", "D"),
            iconSize: [120, 42],
            iconAnchor: [16, 32],
          }),
        }).addTo(layers)
      }

      if (driverCoords) {
        points.push(driverCoords)
        L.marker(driverCoords, {
          icon: L.divIcon({
            className: "",
            html: markerMarkup("Driver", "#d97706", "R"),
            iconSize: [120, 42],
            iconAnchor: [16, 32],
          }),
        }).addTo(layers)
      }

      const routePoints = [pickupCoords, driverCoords, dropoffCoords].filter(
        (value): value is [number, number] => Array.isArray(value)
      )
      if (routePoints.length >= 2) {
        L.polyline(routePoints, {
          color: "#0f172a",
          weight: 3,
          opacity: 0.7,
          dashArray: "8 10",
        }).addTo(layers)
      }

      if (points.length > 1) {
        mapRef.current.fitBounds(L.latLngBounds(points), { padding: [32, 32] })
      } else if (points.length === 1) {
        mapRef.current.setView(points[0], 13)
      } else {
        mapRef.current.setView([20.5937, 78.9629], 4)
      }
    }

    void setup()

    return () => {
      cancelled = true
    }
  }, [driverLocation, dropoff, pickup])

  useEffect(() => {
    return () => {
      mapRef.current?.remove()
      mapRef.current = null
      layerGroupRef.current = null
    }
  }, [])

  return (
    <div className="overflow-hidden rounded-[24px] border border-slate-200 bg-slate-100">
      <div ref={containerRef} className="h-[320px] w-full" />
      <div className="flex flex-wrap items-center gap-3 border-t border-slate-200 bg-white px-4 py-3 text-xs text-slate-500">
        <span className="inline-flex items-center gap-1.5">
          <MapPin className="h-3.5 w-3.5 text-teal-700" />
          Pickup
        </span>
        <span className="inline-flex items-center gap-1.5">
          <MapPin className="h-3.5 w-3.5 text-blue-700" />
          Drop
        </span>
        <span className="inline-flex items-center gap-1.5">
          <Navigation className="h-3.5 w-3.5 text-amber-600" />
          Latest tracked position
        </span>
      </div>
    </div>
  )
}
