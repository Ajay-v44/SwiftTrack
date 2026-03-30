"use client"

import { useState } from "react"
import { createTenantOrderService, fetchTenantOrderQuotesService } from "@swifttrack/services"
import { TenantCreateOrderInput, TenantOrderQuote, TenantOrderQuoteFormInput } from "@swifttrack/types"

export function useTenantOrderCreation() {
  const [quote, setQuote] = useState<TenantOrderQuote | null>(null)
  const [isLoading, setIsLoading] = useState(false)

  async function loadQuotes(input: TenantOrderQuoteFormInput) {
    setIsLoading(true)
    try {
      const response = await fetchTenantOrderQuotesService(input)
      setQuote(response)
      return response
    } finally {
      setIsLoading(false)
    }
  }

  async function dispatchOrder(input: TenantCreateOrderInput) {
    setIsLoading(true)
    try {
      return await createTenantOrderService(input)
    } finally {
      setIsLoading(false)
    }
  }

  return {
    quote,
    isLoading,
    loadQuotes,
    dispatchOrder,
  }
}
