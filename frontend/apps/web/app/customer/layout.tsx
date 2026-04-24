"use client"

import Link from "next/link"
import { usePathname, useRouter } from "next/navigation"
import type React from "react"
import { useEffect } from "react"
import { LayoutDashboard, LogOut, MapPinHouse, PackagePlus, Search } from "lucide-react"
import { Button } from "@/components/ui/button"
import { useAuthStore } from "@/store/useAuthStore"

const navigation = [
  { name: "Dashboard", href: "/customer/dashboard", icon: LayoutDashboard },
  { name: "Create shipment", href: "/customer/orders/create", icon: PackagePlus },
  { name: "Addresses", href: "/customer/addresses", icon: MapPinHouse },
  { name: "Track", href: "/track", icon: Search },
]

export default function CustomerLayout({ children }: { children: React.ReactNode }) {
  const { user, logout } = useAuthStore()
  const router = useRouter()
  const pathname = usePathname()

  useEffect(() => {
    if (!user) {
      router.replace("/login")
      return
    }
    if (user.type !== "CONSUMER") {
      if (user.type?.startsWith("TENANT_") && user.type !== "TENANT_DRIVER") router.replace("/tenant/dashboard")
      else if (user.type === "PROVIDER_USER") router.replace("/provider/dashboard")
      else if (user.type === "TENANT_DRIVER" || user.type === "DRIVER_USER") router.replace("/driver/dashboard")
      else if (user.type === "SUPER_ADMIN" || user.type === "SYSTEM_ADMIN") router.replace("/admin/dashboard")
      else router.replace("/")
    }
  }, [router, user])

  if (!user || user.type !== "CONSUMER") {
    return <div className="flex min-h-screen items-center justify-center bg-slate-50 text-slate-500">Loading...</div>
  }

  return (
    <div className="min-h-screen bg-slate-50 text-slate-950">
      <aside className="fixed left-0 top-0 hidden h-full w-72 border-r border-slate-200 bg-white px-6 py-8 lg:block">
        <div className="mb-10 px-2">
          <div className="text-2xl font-semibold tracking-tight">SwiftTrack</div>
          <p className="mt-1 text-xs uppercase tracking-[0.28em] text-slate-500">Customer</p>
        </div>
        <nav className="flex flex-col gap-2">
          {navigation.map((item) => {
            const active = pathname.startsWith(item.href)
            return (
              <Link
                key={item.href}
                href={item.href}
                className={`flex items-center gap-3 rounded-2xl px-4 py-3 text-sm font-medium transition ${
                  active ? "bg-slate-950 text-white" : "text-slate-600 hover:bg-slate-100 hover:text-slate-950"
                }`}
              >
                <item.icon className="h-4 w-4" />
                {item.name}
              </Link>
            )
          })}
        </nav>
      </aside>
      <main className="min-h-screen lg:ml-72">
        <header className="sticky top-0 z-30 flex h-20 items-center justify-between border-b border-slate-200 bg-white/90 px-4 backdrop-blur sm:px-6 lg:px-8">
          <div>
            <p className="text-sm text-slate-500">Signed in as</p>
            <p className="font-medium">{user.name}</p>
          </div>
          <div className="flex items-center gap-2">
            {navigation.map((item) => (
              <Button key={item.href} asChild variant="outline" size="sm" className="hidden rounded-full bg-white sm:inline-flex lg:hidden">
                <Link href={item.href}>{item.name}</Link>
              </Button>
            ))}
            <Button
              variant="outline"
              size="sm"
              className="rounded-full bg-white"
              onClick={() => {
                logout()
                router.push("/login")
              }}
            >
              <LogOut className="h-4 w-4" />
              Logout
            </Button>
          </div>
        </header>
        {children}
      </main>
    </div>
  )
}
