"use client"

import { useEffect, useState } from "react"
import { fetchTenantFinanceService } from "@swifttrack/services"
import { PaginatedLedgerTransactionsResponse, TenantFinanceSummary } from "@swifttrack/types"

const initialSummary: TenantFinanceSummary = {
  balance: 0,
  weeklySpend: 0,
  costSavings: 0,
  unpaidDues: 0,
  invoiceCount: 0,
}

const initialTransactions: PaginatedLedgerTransactionsResponse = {
  items: [],
  page: 0,
  size: 5,
  totalElements: 0,
  totalPages: 0,
}

export function useTenantFinance(initialPage = 0, pageSize = 5) {
  const [summary, setSummary] = useState<TenantFinanceSummary>(initialSummary)
  const [transactions, setTransactions] = useState<PaginatedLedgerTransactionsResponse>(initialTransactions)
  const [page, setPage] = useState(initialPage)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let active = true

    async function load() {
      setLoading(true)
      setError(null)

      try {
        const response = await fetchTenantFinanceService(page, pageSize)
        if (!active) {
          return
        }

        setSummary(response.summary)
        setTransactions(response.transactions)
      } catch (err) {
        console.error("Tenant finance fetch failed", err)
        if (active) {
          setError("Failed to load finance data")
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
  }, [page, pageSize])

  return {
    summary,
    transactions,
    page,
    setPage,
    loading,
    error,
  }
}
