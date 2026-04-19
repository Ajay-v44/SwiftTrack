"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { AxiosError } from "axios"
import { toast } from "sonner"
import {
  fetchTenantSetupStatusService,
  fetchUserDetailsService,
  loginWithEmailService,
  loginWithMobileOtpService,
  registerUserService,
} from "@swifttrack/services"
import type { TenantRegisterInput, UserDetails } from "@swifttrack/types"
import { useAuthStore } from "@/store/useAuthStore"

export function useLogin() {
  const router = useRouter()
  const { setAuth, setUser } = useAuthStore()
  const [isLoading, setIsLoading] = useState(false)
  const [otpSent, setOtpSent] = useState(false)

  async function resolveTenantRoute(userDetails: UserDetails) {
    const setupStatus = await fetchTenantSetupStatusService()

    if (!setupStatus.setupComplete) {
      router.push(`/tenant/setup?step=${setupStatus.nextStep}`)
      return
    }

    router.push("/tenant/dashboard")
  }

  async function handleUserBootstrap(token: string) {
    const userDetails = await fetchUserDetailsService(token)
    setUser(userDetails)
    toast.success(`Welcome back, ${userDetails.name}!`)

    if (userDetails.type === "SUPER_ADMIN" || userDetails.type === "SYSTEM_ADMIN") {
      router.push("/admin/dashboard")
      return
    }

    if (
      userDetails.type === "TENANT_ADMIN" ||
      userDetails.type === "TENANT_MANAGER" ||
      userDetails.type === "TENANT_USER"
    ) {
      await resolveTenantRoute(userDetails)
      return
    }

    if (userDetails.type === "PROVIDER_USER") {
      router.push("/provider/dashboard")
      return
    }

    if (userDetails.type === "TENANT_DRIVER" || userDetails.type === "DRIVER_USER") {
      router.push("/driver/dashboard")
      return
    }

    if (userDetails.type === "CONSUMER") {
      router.push("/track")
      return
    }

    router.push("/")
  }

  async function loginWithEmail(email: string, password: string) {
    setIsLoading(true)
    try {
      const { accessToken } = await loginWithEmailService(email, password)
      setAuth(accessToken)
      await handleUserBootstrap(accessToken)
    } catch (error: unknown) {
      const message = getErrorMessage(error, "Invalid credentials")
      toast.error(message)
      throw error
    } finally {
      setIsLoading(false)
    }
  }

  async function registerUser(payload: TenantRegisterInput & { userType: UserDetails["type"] }) {
    setIsLoading(true)
    try {
      await registerUserService(payload)
      toast.success("Account created successfully")
      const { accessToken } = await loginWithEmailService(payload.email, payload.password)
      setAuth(accessToken)
      await handleUserBootstrap(accessToken)
    } catch (error: unknown) {
      const message = getErrorMessage(error, "Failed to register")
      toast.error(message)
      throw error
    } finally {
      setIsLoading(false)
    }
  }

  async function loginWithPhone(mobileNum: string, otp: string) {
    setIsLoading(true)
    try {
      if (!otpSent) {
        const response = await loginWithMobileOtpService(mobileNum, null)
        if (response.tokenType === "OTP_SENT") {
          setOtpSent(true)
          toast.success("OTP sent successfully to your mobile number")
        }
        return
      }

      const { accessToken } = await loginWithMobileOtpService(mobileNum, otp)
      setAuth(accessToken)
      await handleUserBootstrap(accessToken)
    } catch (error: unknown) {
      const message = getErrorMessage(error, "Error logging in")
      toast.error(message)
      throw error
    } finally {
      setIsLoading(false)
    }
  }

  return {
    isLoading,
    otpSent,
    loginWithEmail,
    loginWithPhone,
    registerUser,
  }
}
  function getErrorMessage(error: unknown, fallback: string) {
    if (error instanceof AxiosError) {
      return (error.response?.data as { message?: string } | undefined)?.message || fallback
    }
    return error instanceof Error ? error.message : fallback
  }
