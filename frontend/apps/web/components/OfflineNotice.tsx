"use client"

import { useEffect, useState } from "react"
import { CloudOff, Wifi } from "lucide-react"

export function OfflineNotice() {
  const [isOnline, setIsOnline] = useState(true)

  useEffect(() => {
    setIsOnline(window.navigator.onLine)

    function handleOnline() {
      setIsOnline(true)
    }

    function handleOffline() {
      setIsOnline(false)
    }

    window.addEventListener("online", handleOnline)
    window.addEventListener("offline", handleOffline)

    return () => {
      window.removeEventListener("online", handleOnline)
      window.removeEventListener("offline", handleOffline)
    }
  }, [])

  if (isOnline) {
    return null
  }

  return (
    <div className="pointer-events-none fixed inset-x-0 top-4 z-[100] flex justify-center px-4">
      <div className="pointer-events-auto flex max-w-md items-center gap-3 rounded-full border border-rose-200 bg-white/95 px-4 py-3 text-sm text-rose-700 shadow-lg backdrop-blur-md">
        <span className="inline-flex h-10 w-10 items-center justify-center rounded-full bg-rose-50 text-rose-600">
          <CloudOff className="h-5 w-5" />
        </span>
        <div className="min-w-0">
          <p className="font-semibold">You&apos;re offline right now</p>
          <p className="text-xs text-rose-600/90">Some live updates may pause until your internet comes back.</p>
        </div>
        <Wifi className="h-4 w-4 shrink-0 text-rose-400" />
      </div>
    </div>
  )
}
