"use client"

import { startTransition, useDeferredValue, useEffect, useMemo, useState } from "react"
import { fetchTenantOrdersService } from "@swifttrack/services"
import { PaginatedTenantOrdersResponse } from "@swifttrack/types"

const initialOrders: PaginatedTenantOrdersResponse = {
  items: [],
  page: 0,
  size: 10,
  totalElements: 0,
  totalPages: 0,
  summary: {
    processedOrders: 0,
    openIssues: 0,
    deliveredOrders: 0,
    activeOrders: 0,
  },
}

function createDefaultDateRange() {
  const end = new Date()
  const start = new Date()
  start.setDate(end.getDate() - 29)

  return {
    startDate: start.toISOString().slice(0, 10),
    endDate: end.toISOString().slice(0, 10),
  }
}

export function useTenantOrders() {
  const [orders, setOrders] = useState<PaginatedTenantOrdersResponse>(initialOrders)
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(10)
  const [query, setQuery] = useState("")
  const [dateRange, setDateRange] = useState(createDefaultDateRange())
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [reloadKey, setReloadKey] = useState(0)
  const deferredQuery = useDeferredValue(query)

  useEffect(() => {
    startTransition(() => {
      setPage(0)
    })
  }, [deferredQuery, dateRange.endDate, dateRange.startDate])

  useEffect(() => {
    let active = true

    async function load() {
      setLoading(true)
      setError(null)

      try {
        const response = await fetchTenantOrdersService({
          page,
          size,
          query: deferredQuery,
          startDate: dateRange.startDate,
          endDate: dateRange.endDate,
        })

        if (active) {
          setOrders(response)
        }
      } catch (err) {
        console.error("Tenant orders fetch failed", err)
        if (active) {
          setError("Failed to load orders")
        }
      } finally {
        if (active) {
          setLoading(false)
        }
      }
    }

    load()

    return () => {
      active = false
    }
  }, [dateRange.endDate, dateRange.startDate, deferredQuery, page, reloadKey, size])

  const pagination = useMemo(
    () => ({
      page,
      size,
      totalPages: orders.totalPages,
      totalElements: orders.totalElements,
    }),
    [orders.totalElements, orders.totalPages, page, size]
  )

  return {
    orders,
    loading,
    error,
    query,
    setQuery,
    dateRange,
    setDateRange,
    page,
    setPage,
    size,
    setSize,
    pagination,
    refresh: () => setReloadKey((current) => current + 1),
  }
}
