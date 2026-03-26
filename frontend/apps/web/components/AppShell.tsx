"use client"

import { ReactNode } from "react"
import { usePathname } from "next/navigation"
import { Navbar } from "@/components/Navbar"
import { Footer } from "@/components/Footer"

const APP_ROUTE_PREFIXES = ["/tenant", "/admin", "/provider", "/driver"]

export function AppShell({ children }: { children: ReactNode }) {
  const pathname = usePathname()
  const isAppRoute = APP_ROUTE_PREFIXES.some((prefix) => pathname.startsWith(prefix))

  return (
    <>
      {!isAppRoute && <Navbar />}
      {children}
      {!isAppRoute && <Footer />}
    </>
  )
}
