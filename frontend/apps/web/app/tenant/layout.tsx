"use client"

import React, { useEffect } from "react"
import { useRouter, usePathname } from "next/navigation"
import TenantSidebar from "@/components/tenant/TenantSidebar"
import TenantHeader from "@/components/tenant/TenantHeader"
import { TenantNotificationsProvider } from "@/components/tenant/TenantNotificationsProvider"
import { useTenantSetupGuard } from "@/hooks/useTenantSetupGuard"
import { useAuthStore } from "@/store/useAuthStore"
import { Loader2 } from "lucide-react"

export default function TenantLayout({
  children,
}: {
  children: React.ReactNode
}) {
  const router = useRouter()
  const pathname = usePathname()
  const { setupStatus, loading } = useTenantSetupGuard()
  const { user } = useAuthStore()

  useEffect(() => {
    if (!user) {
      router.replace("/login")
      return
    }

    const isTenant = user.type?.startsWith("TENANT_") && user.type !== "TENANT_DRIVER"
    if (!isTenant) {
      if (user.type === "PROVIDER_USER") router.replace("/provider/dashboard")
      else if (user.type === "TENANT_DRIVER" || user.type === "DRIVER_USER") router.replace("/driver/dashboard")
      else if (user.type === "SUPER_ADMIN" || user.type === "SYSTEM_ADMIN") router.replace("/admin/dashboard")
      else if (user.type === "CONSUMER") router.replace("/track")
      else router.replace("/")
      return
    }

    if (!loading && setupStatus && !setupStatus.setupComplete) {
      const onSetupPage = pathname?.includes("/tenant/setup")
      if (!onSetupPage) {
        router.replace(`/tenant/setup?step=${setupStatus.nextStep || "company"}`)
      }
    }
  }, [loading, setupStatus, pathname, router, user])

  const isTenant = user?.type?.startsWith("TENANT_") && user?.type !== "TENANT_DRIVER"

  if (loading || !user || !isTenant) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-50">
        <Loader2 className="h-8 w-8 animate-spin text-slate-400" />
      </div>
    )
  }

  return (
    <TenantNotificationsProvider>
      <div className="min-h-screen bg-slate-50 text-slate-950 antialiased">
        <TenantSidebar />
        <main className="min-h-screen flex flex-col lg:ml-72">
          <TenantHeader />
          {children}
        </main>
      </div>
    </TenantNotificationsProvider>
  )
}
