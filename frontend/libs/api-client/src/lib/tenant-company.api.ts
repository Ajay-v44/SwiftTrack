import { httpClient } from "./http-client"
import { serviceEndpoints } from "./endpoints"
import { CompanyRegistrationInput } from "@swifttrack/types"

export function registerTenantCompanyApi(userId: string, payload: CompanyRegistrationInput) {
  return httpClient.post(`${serviceEndpoints.tenantCompany}/v1/register`, payload, {
    params: { id: userId },
  })
}
