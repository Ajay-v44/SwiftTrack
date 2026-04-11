"use client"

import { useEffect, useState } from "react"
import { usePathname } from "next/navigation"
import { fetchTenantSetupStatusService } from "@swifttrack/services"
import type { TenantSetupStatus } from "@swifttrack/types"

export function useTenantSetupGuard() {
  const pathname = usePathname()
  const [setupStatus, setSetupStatus] = useState<TenantSetupStatus | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let active = true

    setLoading(true)

    async function load() {
      try {
        const status = await fetchTenantSetupStatusService()
        if (active) {
          setSetupStatus(status)
        }
      } catch {
        if (active) {
          setSetupStatus(null)
        }
      } finally {
        if (active) {
          setLoading(false)
        }
      }
    }

    void load()

    return () => {
      active = false
    }
  }, [pathname])

  return {
    loading,
    setupStatus,
    canCreateOrder: Boolean(setupStatus?.setupComplete),
    createOrderHref: setupStatus?.setupComplete ? "/tenant/orders/create" : `/tenant/setup?step=${setupStatus?.nextStep ?? "company"}`,
  }
}
