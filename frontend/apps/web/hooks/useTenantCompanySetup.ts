"use client"

import { useState } from "react"
import { registerTenantCompanyService } from "@swifttrack/services"
import { CompanyRegistrationInput } from "@swifttrack/types"

export function useTenantCompanySetup() {
  const [isLoading, setIsLoading] = useState(false)

  async function submit(userId: string, payload: CompanyRegistrationInput) {
    setIsLoading(true)
    try {
      await registerTenantCompanyService(userId, payload)
    } finally {
      setIsLoading(false)
    }
  }

  return {
    isLoading,
    submit,
  }
}
