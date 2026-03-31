import {
  fetchTenantSetupStatusApi,
  fetchUserDetailsApi,
  loginWithEmailApi,
  loginWithMobileOtpApi,
  registerTenantApi,
} from "@swifttrack/api-client"
import { LoginResponse, TenantRegisterInput, TenantSetupStatus, UserDetails } from "@swifttrack/types"

export async function registerTenantService(payload: TenantRegisterInput): Promise<string> {
  const response = await registerTenantApi(payload)
  return response.data?.message ?? "User registered Successfully"
}

export async function loginWithEmailService(email: string, password: string): Promise<LoginResponse> {
  const response = await loginWithEmailApi(email, password)
  return response.data
}

export async function loginWithMobileOtpService(
  mobileNum: string,
  otp: string | null
): Promise<LoginResponse> {
  const response = await loginWithMobileOtpApi(mobileNum, otp)
  return response.data
}

export async function fetchUserDetailsService(token: string): Promise<UserDetails> {
  const response = await fetchUserDetailsApi(token)
  return response.data
}

export async function fetchTenantSetupStatusService(): Promise<TenantSetupStatus> {
  const response = await fetchTenantSetupStatusApi()
  return response.data
}
