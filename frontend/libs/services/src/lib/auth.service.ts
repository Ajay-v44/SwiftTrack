import {
  fetchUserDetailsApi,
  loginWithEmailApi,
  loginWithMobileOtpApi,
} from "@swifttrack/api-client"
import { LoginResponse, UserDetails } from "@swifttrack/types"

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
