import React from "react"
import TenantSidebar from "@/components/tenant/TenantSidebar"
import TenantHeader from "@/components/tenant/TenantHeader"
import { TenantNotificationsProvider } from "@/components/tenant/TenantNotificationsProvider"

export default function TenantLayout({
  children,
}: {
  children: React.ReactNode
}) {
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
