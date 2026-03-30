"use client"

import Link from "next/link"
import { usePathname } from "next/navigation"
import { LayoutDashboard, MapPinHouse, Package, Users, Wallet } from "lucide-react"
import { useAuthStore } from "@/store/useAuthStore"

export default function TenantSidebar() {
  const pathname = usePathname()
  const { user } = useAuthStore()

  const navigation = [
    { name: "Dashboard", href: "/tenant/dashboard", icon: LayoutDashboard },
    { name: "Orders", href: "/tenant/orders", icon: Package },
    { name: "Addresses", href: "/tenant/addresses", icon: MapPinHouse },
    { name: "Finance", href: "/tenant/finance", icon: Wallet },
    { name: "Team Management", href: "/tenant/team", icon: Users },
  ]

  return (
    <aside className="fixed left-0 top-0 z-50 hidden h-full w-72 flex-col border-r border-slate-200 bg-white px-6 py-8 lg:flex">
      <div className="mb-10 px-2">
        <div className="text-2xl font-semibold tracking-tight text-slate-950">SwiftTrack</div>
        <p className="mt-1 text-xs uppercase tracking-[0.28em] text-slate-500">Tenant Workspace</p>
      </div>

      <nav className="flex flex-1 flex-col gap-2">
        {navigation.map((item) => {
          const isActive = pathname.startsWith(item.href)

          return (
            <Link
              key={item.name}
              href={item.href}
              className={`flex items-center gap-3 rounded-2xl px-4 py-3 text-sm font-medium transition ${
                isActive
                  ? "bg-slate-950 text-white shadow-sm"
                  : "text-slate-600 hover:bg-slate-100 hover:text-slate-950"
              }`}
            >
              <item.icon className="h-4 w-4" />
              <span>{item.name}</span>
            </Link>
          )
        })}
      </nav>

      <div className="rounded-3xl border border-slate-200 bg-slate-50 p-4">
        <p className="text-xs uppercase tracking-[0.24em] text-slate-500">Tenant</p>
        <p className="mt-2 text-sm font-medium text-slate-950">{user?.name || "Loading user"}</p>
        <p className="mt-1 text-sm text-slate-500">{user?.type?.replaceAll("_", " ") || "TENANT ADMIN"}</p>
      </div>
    </aside>
  )
}
