"use client"

import { useState } from "react"
import { dispatchTenantOrderService, fetchTenantOrderQuotesService } from "@swifttrack/services"
import { TenantOrderQuote, TenantOrderQuoteFormInput } from "@swifttrack/types"

export function useTenantOrderCreation() {
  const [quotes, setQuotes] = useState<TenantOrderQuote[]>([])
  const [isLoading, setIsLoading] = useState(false)

  async function loadQuotes(input: TenantOrderQuoteFormInput) {
    setIsLoading(true)
    try {
      const response = await fetchTenantOrderQuotesService(input)
      setQuotes(response)
      return response
    } finally {
      setIsLoading(false)
    }
  }

  async function dispatchOrder(quoteId: string) {
    setIsLoading(true)
    try {
      await dispatchTenantOrderService(quoteId)
    } finally {
      setIsLoading(false)
    }
  }

  return {
    quotes,
    isLoading,
    loadQuotes,
    dispatchOrder,
  }
}
