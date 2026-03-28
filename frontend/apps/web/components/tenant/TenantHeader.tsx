"use client"

import Link from "next/link"
import { usePathname, useRouter } from "next/navigation"
import { Bell, LayoutDashboard, LogOut, Menu, Package, Search, Users, Wallet } from "lucide-react"
import { useAuthStore } from "@/store/useAuthStore"
import { Button } from "@/components/ui/button"
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle, SheetTrigger } from "@/components/ui/sheet"

export default function TenantHeader() {
  const { user, logout } = useAuthStore()
  const router = useRouter()
  const pathname = usePathname()

  const navigation = [
    { name: "Dashboard", href: "/tenant/dashboard", icon: LayoutDashboard },
    { name: "Orders", href: "/tenant/orders", icon: Package },
    { name: "Finance", href: "/tenant/finance", icon: Wallet },
    { name: "Team Management", href: "/tenant/team", icon: Users },
  ]

  const handleLogout = () => {
    logout()
    router.push("/login")
  }

  return (
    <header className="sticky top-0 z-40 flex h-20 items-center justify-between border-b border-slate-200 bg-white/90 px-4 backdrop-blur sm:px-6 lg:px-8">
      <div className="flex min-w-0 flex-1 items-center gap-3">
        <div className="lg:hidden">
          <Sheet>
            <SheetTrigger asChild>
              <Button variant="outline" size="icon" className="rounded-full border-slate-200 bg-white">
                <Menu className="h-4 w-4" />
              </Button>
            </SheetTrigger>
            <SheetContent side="left" className="w-[86vw] max-w-sm border-r border-slate-200 bg-white p-0">
              <SheetHeader className="border-b border-slate-200 px-6 py-6 text-left">
                <SheetTitle className="text-slate-950">SwiftTrack</SheetTitle>
                <SheetDescription>Tenant workspace navigation</SheetDescription>
              </SheetHeader>
              <div className="flex h-full flex-col px-4 py-6">
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
              </div>
            </SheetContent>
          </Sheet>
        </div>

        <div className="flex w-full min-w-0 max-w-xl items-center gap-3 rounded-full border border-slate-200 bg-slate-50 px-4 py-2.5">
          <Search className="h-4 w-4 shrink-0 text-slate-400" />
          <input
            className="w-full min-w-0 bg-transparent text-sm text-slate-700 outline-none placeholder:text-slate-400"
            placeholder="Search shipments, orders, or tracking IDs"
            type="text"
          />
        </div>
      </div>

      <div className="ml-3 flex shrink-0 items-center gap-2 sm:ml-6 sm:gap-4">
        <button className="relative rounded-full border border-slate-200 bg-white p-2.5 text-slate-600 transition hover:bg-slate-50">
          <Bell className="h-4 w-4" />
          <span className="absolute right-2 top-2 h-2 w-2 rounded-full bg-emerald-500" />
        </button>

        <div className="hidden text-right sm:block">
          <p className="text-sm font-medium text-slate-950">{user?.name || "Tenant User"}</p>
          <p className="text-xs text-slate-500">{user?.roles?.[0] || user?.type || "Operations"}</p>
        </div>

        <div className="flex items-center gap-2 rounded-full border border-slate-200 bg-white px-2 py-1.5">
          <div className="flex h-9 w-9 items-center justify-center rounded-full bg-slate-950 text-sm font-semibold text-white">
            {(user?.name || "TU")
              .split(" ")
              .map((part) => part[0])
              .join("")
              .slice(0, 2)
              .toUpperCase()}
          </div>
          <button
            onClick={handleLogout}
            className="rounded-full p-2 text-slate-500 transition hover:bg-slate-100 hover:text-slate-950"
          >
            <LogOut className="h-4 w-4" />
          </button>
        </div>
      </div>
    </header>
  )
}
