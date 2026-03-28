import { registerTenantCompanyApi } from "@swifttrack/api-client"
import { CompanyRegistrationInput } from "@swifttrack/types"

export async function registerTenantCompanyService(
  userId: string,
  payload: CompanyRegistrationInput
) {
  return registerTenantCompanyApi(userId, payload)
}
