"use client"

import { createContext, useContext, useEffect, useMemo, useState } from "react"
import { fetchTenantNotificationsService } from "@swifttrack/services"
import type { TenantDashboardNotification } from "@swifttrack/types"

interface TenantNotificationsContextValue {
  notifications: TenantDashboardNotification[]
  unreadCount: number
  loading: boolean
  isOpen: boolean
  setIsOpen: (value: boolean) => void
  deleteNotification: (id: string) => void
}

const TenantNotificationsContext = createContext<TenantNotificationsContextValue | null>(null)

export function TenantNotificationsProvider({
  children,
}: {
  children: React.ReactNode
}) {
  const [notifications, setNotifications] = useState<TenantDashboardNotification[]>([])
  const [loading, setLoading] = useState(true)
  const [isOpen, setIsOpen] = useState(false)

  useEffect(() => {
    let active = true

    async function loadNotifications() {
      setLoading(true)

      try {
        const response = await fetchTenantNotificationsService()
        if (active) {
          setNotifications(response)
        }
      } catch (error) {
        console.error("Tenant notifications fetch failed", error)
      } finally {
        if (active) {
          setLoading(false)
        }
      }
    }

    loadNotifications()
    const intervalId = window.setInterval(loadNotifications, 60_000)

    return () => {
      active = false
      window.clearInterval(intervalId)
    }
  }, [])

  useEffect(() => {
    if (!isOpen) {
      return
    }

    setNotifications((current) =>
      current.map((notification) =>
        notification.unread ? { ...notification, unread: false } : notification
      )
    )
  }, [isOpen])

  const unreadCount = useMemo(
    () => notifications.filter((notification) => notification.unread).length,
    [notifications]
  )

  function deleteNotification(id: string) {
    setNotifications((current) => current.filter((notification) => notification.id !== id))
  }

  return (
    <TenantNotificationsContext.Provider
      value={{
        notifications,
        unreadCount,
        loading,
        isOpen,
        setIsOpen,
        deleteNotification,
      }}
    >
      {children}
    </TenantNotificationsContext.Provider>
  )
}

export function useTenantNotifications() {
  const context = useContext(TenantNotificationsContext)

  if (!context) {
    throw new Error("useTenantNotifications must be used within TenantNotificationsProvider")
  }

  return context
}
