import {
  fetchTenantSetupStatusApi,
  fetchUserDetailsApi,
  loginWithEmailApi,
  loginWithMobileOtpApi,
  registerUserApi,
} from "@swifttrack/api-client"
import { LoginResponse, TenantRegisterInput, TenantSetupStatus, UserDetails } from "@swifttrack/types"

export async function registerUserService(payload: TenantRegisterInput & { userType: UserDetails["type"] }): Promise<string> {
  const response = await registerUserApi(payload)
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
  // The backend returns userType, but the frontend interface expects type
  if (response.data && (response.data as any).userType && !response.data.type) {
    response.data.type = (response.data as any).userType;
  }
  return response.data
}

export async function fetchTenantSetupStatusService(): Promise<TenantSetupStatus> {
  const response = await fetchTenantSetupStatusApi()
  return response.data
}
