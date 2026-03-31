import { httpClient } from "./http-client"
import { serviceEndpoints } from "./endpoints"
import { CompanyRegistrationInput } from "@swifttrack/types"

export function registerTenantCompanyApi(userId: string, payload: CompanyRegistrationInput) {
  const normalizedTenantCode = payload.legalName
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "")
    .slice(0, 40) || `tenant-${userId.slice(0, 8)}`

  return httpClient.post(`${serviceEndpoints.tenantCompany}/v1/register`, {
    tenantCode: `${normalizedTenantCode}-${userId.slice(0, 6).toLowerCase()}`,
    organizationName: payload.legalName.trim(),
    organizationEmail: "",
    organizationPhone: "",
    organizationAddress: "",
    organizationWebsite: "",
    organizationState: "",
    organizationCity: "",
    organizationCountry: "India",
    gstNumber: payload.registrationNumber.trim(),
    cinNumber: payload.registrationNumber.trim(),
    panNumber: "",
    logoUrl: "",
    themeColor: "#3e5bf2",
  }, {
    params: { id: userId },
  })
}
