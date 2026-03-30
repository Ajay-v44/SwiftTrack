"use client"

import Link from "next/link"
import { usePathname, useRouter } from "next/navigation"
import { Bell, LayoutDashboard, LogOut, MapPinHouse, Menu, Package, Search, Trash2, Users, Wallet } from "lucide-react"
import { useAuthStore } from "@/store/useAuthStore"
import { useTenantNotifications } from "@/components/tenant/TenantNotificationsProvider"
import { Button } from "@/components/ui/button"
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle, SheetTrigger } from "@/components/ui/sheet"

export default function TenantHeader() {
  const { user, logout } = useAuthStore()
  const { notifications, unreadCount, loading, isOpen, setIsOpen, deleteNotification } = useTenantNotifications()
  const router = useRouter()
  const pathname = usePathname()

  const navigation = [
    { name: "Dashboard", href: "/tenant/dashboard", icon: LayoutDashboard },
    { name: "Orders", href: "/tenant/orders", icon: Package },
    { name: "Addresses", href: "/tenant/addresses", icon: MapPinHouse },
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
        <Sheet open={isOpen} onOpenChange={setIsOpen}>
          <SheetTrigger asChild>
            <button className="relative rounded-full border border-slate-200 bg-white p-2.5 text-slate-600 transition hover:bg-slate-50">
              <Bell className="h-4 w-4" />
              {unreadCount > 0 ? (
                <span className="absolute -right-1 -top-1 inline-flex min-h-5 min-w-5 items-center justify-center rounded-full bg-emerald-500 px-1 text-[10px] font-semibold text-white">
                  {unreadCount > 9 ? "9+" : unreadCount}
                </span>
              ) : null}
            </button>
          </SheetTrigger>
          <SheetContent side="right" className="w-full max-w-md border-l border-slate-200 bg-white p-0">
            <SheetHeader className="border-b border-slate-200 px-6 py-5 text-left">
              <SheetTitle className="text-slate-950">Notifications</SheetTitle>
              <SheetDescription>
                {unreadCount > 0 ? `${unreadCount} unread updates` : "All caught up"}
              </SheetDescription>
            </SheetHeader>

            <div className="flex-1 overflow-y-auto px-4 py-4">
              {loading ? (
                <div className="space-y-3">
                  {Array.from({ length: 4 }, (_, index) => (
                    <div key={index} className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                      <div className="h-4 w-24 animate-pulse rounded-full bg-slate-200" />
                      <div className="mt-3 h-4 w-40 animate-pulse rounded-full bg-slate-200" />
                      <div className="mt-2 h-3 w-full animate-pulse rounded-full bg-slate-200" />
                    </div>
                  ))}
                </div>
              ) : notifications.length > 0 ? (
                <div className="space-y-3">
                  {notifications.map((notification) => (
                    <div
                      key={notification.id}
                      className={`rounded-2xl border p-4 transition ${
                        notification.unread ? "border-indigo-200 bg-indigo-50/50" : "border-slate-200 bg-slate-50"
                      }`}
                    >
                      <div className="flex items-start gap-3">
                        <div className="min-w-0 flex-1 space-y-2">
                          <div className="flex items-center gap-2">
                            <span className="rounded-full bg-white px-2 py-1 text-[11px] font-medium uppercase text-slate-600">
                              {notification.severity}
                            </span>
                            <span className="text-xs text-slate-500">{formatRelativeTime(notification.createdAt)}</span>
                            {notification.unread ? (
                              <span className="rounded-full bg-emerald-500 px-2 py-1 text-[10px] font-semibold uppercase tracking-[0.18em] text-white">
                                New
                              </span>
                            ) : null}
                          </div>
                          <div className="space-y-1">
                            <p className="text-sm font-medium text-slate-950">{notification.title}</p>
                            <p className="text-sm leading-6 text-slate-600">{notification.message}</p>
                          </div>
                          {notification.actionLabel ? (
                            <p className="text-xs font-medium uppercase tracking-[0.18em] text-slate-500">
                              {notification.actionLabel}
                            </p>
                          ) : null}
                        </div>
                        <button
                          type="button"
                          onClick={() => deleteNotification(notification.id)}
                          className="rounded-full border border-slate-200 bg-white p-2 text-slate-500 transition hover:border-rose-200 hover:bg-rose-50 hover:text-rose-600"
                          aria-label={`Delete ${notification.title}`}
                        >
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="rounded-3xl border border-dashed border-slate-200 bg-slate-50 px-6 py-10 text-center">
                  <p className="text-sm font-medium text-slate-900">No notifications left</p>
                  <p className="mt-2 text-sm text-slate-500">New tenant updates will appear here.</p>
                </div>
              )}
            </div>
          </SheetContent>
        </Sheet>

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

function formatRelativeTime(date: string) {
  const diff = Date.now() - new Date(date).getTime()
  const minutes = Math.max(Math.round(diff / 60_000), 0)

  if (minutes < 1) {
    return "just now"
  }

  if (minutes < 60) {
    return `${minutes}m ago`
  }

  const hours = Math.round(minutes / 60)
  if (hours < 24) {
    return `${hours}h ago`
  }

  const days = Math.round(hours / 24)
  return `${days}d ago`
}
