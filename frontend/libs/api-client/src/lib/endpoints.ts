const FALLBACK_GATEWAY_URL = "http://localhost:8080"

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

export const gatewayBaseUrl = normalizeGatewayBaseUrl(process.env["NEXT_PUBLIC_API_URL"])

export const serviceEndpoints = {
  auth: `${gatewayBaseUrl}/authservice/api/users`,
  billingAccounts: `${gatewayBaseUrl}/billingandsettlementservice/api/accounts`,
  orders: `${gatewayBaseUrl}/orderservice/api/order`,
  tenantCompany: `${gatewayBaseUrl}/tenantservice/company`,
} as const
