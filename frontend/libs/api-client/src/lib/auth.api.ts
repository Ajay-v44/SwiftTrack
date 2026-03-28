import { httpClient } from "./http-client"
import { serviceEndpoints } from "./endpoints"
import { LoginResponse, UserDetails } from "@swifttrack/types"

export function loginWithEmailApi(email: string, password: string) {
  return httpClient.post<LoginResponse>(`${serviceEndpoints.auth}/v1/login/emailAndPassword`, {
    email,
    password,
  })
}

export function loginWithMobileOtpApi(mobileNum: string, otp: string | null) {
  return httpClient.post<LoginResponse>(`${serviceEndpoints.auth}/v1/login/mobileNumAndOtp`, {
    mobileNum,
    otp,
  })
}

export function fetchUserDetailsApi(token: string) {
  return httpClient.post<UserDetails>(`${serviceEndpoints.auth}/v1/getUserDetails`, null, {
    params: { token },
  })
}
