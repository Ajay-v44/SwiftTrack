const FALLBACK_GATEWAY_URL = "http://localhost:8080"
const FALLBACK_PROVIDER_SERVICE_URL = "http://localhost:8004"

function normalizeGatewayBaseUrl(input?: string) {
  const rawValue = input?.replace(/\/+$/, "")
  if (!rawValue) {
    return FALLBACK_GATEWAY_URL
  }

  return rawValue
    .replace(/\/authservice\/api\/users$/, "")
    .replace(/\/billingandsettlementservice\/api\/accounts$/, "")
    .replace(/\/orderservice\/api\/order$/, "")
    .replace(/\/tenantservice\/company$/, "")
}

function normalizeServiceBaseUrl(input: string | undefined, fallback: string) {
  const rawValue = input?.replace(/\/+$/, "")
  return rawValue || fallback
}

export const gatewayBaseUrl = normalizeGatewayBaseUrl(process.env["NEXT_PUBLIC_API_URL"])
export const providerServiceBaseUrl = normalizeServiceBaseUrl(
  process.env["NEXT_PUBLIC_PROVIDER_API_URL"],
  FALLBACK_PROVIDER_SERVICE_URL
)

export const serviceEndpoints = {
  authBase: `${gatewayBaseUrl}/authservice`,
  auth: `${gatewayBaseUrl}/authservice/api/users`,
  authRoles: `${gatewayBaseUrl}/authservice/user-role`,
  authAssignRole: `${gatewayBaseUrl}/authservice/api/assignRole`,
  billingAccounts: `${gatewayBaseUrl}/billingandsettlementservice/api/accounts`,
  mapService: `${gatewayBaseUrl}/mapservice`,
  orders: `${gatewayBaseUrl}/orderservice/api/order`,
  tenantService: `${gatewayBaseUrl}/tenantservice`,
  tenantCompany: `${gatewayBaseUrl}/tenantservice/company`,
  tenantDelivery: `${gatewayBaseUrl}/tenantservice/tenant-delivery`,
  tenantProviders: `${gatewayBaseUrl}/tenantservice/api/providers`,
  providerService: `${providerServiceBaseUrl}/api/providers`,
} as const
