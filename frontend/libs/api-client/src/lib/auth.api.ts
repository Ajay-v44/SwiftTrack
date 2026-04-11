import { httpClient } from "./http-client"
import { serviceEndpoints } from "./endpoints"
import { LoginResponse, TenantRegisterInput, TenantSetupStatus, UserDetails } from "@swifttrack/types"

export function registerUserApi(payload: TenantRegisterInput & { userType: UserDetails["type"] }) {
  return httpClient.post<{ message?: string }>(`${serviceEndpoints.auth}/v1/register`, payload)
}

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

export function fetchTenantSetupStatusApi() {
  return httpClient.get<TenantSetupStatus>(`${serviceEndpoints.tenantService}/api/tenant/v1/setup-status`)
}
