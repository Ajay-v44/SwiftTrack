import { getAuthTokenFromBrowser, httpClient } from "./http-client"
import { serviceEndpoints } from "./endpoints"
import type {
  AssignRolesInput,
  CreateManagedUserInput,
  CreateRoleInput,
  DeliveryOptionResponse,
  PaginatedTenantUsersResponse,
  ProviderSummary,
  RoleViewResponse,
  TenantDeliveryConf,
  TenantDeliveryPriorityInput,
  TenantProviderConfigSummary,
  TenantUserTypeGroup,
  TenantUsersQuery,
  UpdateUserStatusVerificationInput,
  UserType,
} from "@swifttrack/types"

function getRequiredToken() {
  const token = getAuthTokenFromBrowser()
  if (!token) {
    throw new Error("Missing auth token")
  }
  return token
}

export function fetchTenantUsersApi(query: TenantUsersQuery) {
  const token = getRequiredToken()

  return httpClient.post<PaginatedTenantUsersResponse>(`${serviceEndpoints.auth}/v1/getTenantUsers`, null, {
    params: {
      token,
      page: query.page ?? 0,
      size: query.size ?? 10,
      query: query.query || undefined,
      userTypes: query.userTypes?.length ? query.userTypes.join(",") : undefined,
      includeRequestingUser: query.includeRequestingUser ?? false,
    },
  })
}

export function fetchLegacyTenantUsersByTypeApi(userType: UserType) {
  const token = getRequiredToken()

  return httpClient.post<Array<{
    id: string
    name: string
    mobile: string
    email: string
    status: boolean
    userType: UserType
  }>>(`${serviceEndpoints.auth}/v1/getTenantUsers`, null, {
    params: {
      token,
      userType,
    },
  })
}

export function fetchTenantUserTypesApi() {
  return httpClient.get<TenantUserTypeGroup[]>(`${serviceEndpoints.auth}/v1/user-types`)
}

export function fetchRolesApi(status = true) {
  return httpClient.get<RoleViewResponse[]>(`${serviceEndpoints.authRoles}/v1`, {
    params: { status },
  })
}

export function createRoleApi(payload: CreateRoleInput) {
  return httpClient.post<string>(`${serviceEndpoints.authRoles}/v1`, payload)
}

export function createManagedUserApi(payload: CreateManagedUserInput) {
  const token = getRequiredToken()

  return httpClient.post(`${serviceEndpoints.auth}/v1/admin/createManagedUser`, payload, {
    params: { token },
  })
}

export function assignRolesApi(payload: AssignRolesInput) {
  const token = getRequiredToken()

  return httpClient.post(`${serviceEndpoints.authAssignRole}/v1/add`, payload, {
    params: { token },
  })
}

export function updateUserStatusApi(payload: UpdateUserStatusVerificationInput) {
  const token = getRequiredToken()

  return httpClient.post(`${serviceEndpoints.auth}/v1/updateUserStatusAndVerification`, payload, {
    params: { token },
  })
}

export function fetchActiveDeliveryOptionsApi() {
  return httpClient.get<DeliveryOptionResponse[]>(`${serviceEndpoints.tenantDelivery}/v1/options/active`)
}

export function fetchTenantDeliveryConfigurationApi() {
  return httpClient.get<TenantDeliveryConf[]>(`${serviceEndpoints.tenantDelivery}/v1/configure`)
}

export function saveTenantDeliveryConfigurationApi(payload: TenantDeliveryPriorityInput[]) {
  return httpClient.post(`${serviceEndpoints.tenantDelivery}/v1/configure`, payload)
}

export function fetchAvailableProvidersApi() {
  return httpClient.get<ProviderSummary[]>(`${serviceEndpoints.tenantProviders}/v1/getProviders`)
}

export function fetchConfiguredTenantProvidersApi() {
  return httpClient.get<ProviderSummary[]>(`${serviceEndpoints.providerService}/v1/getTenantProviders`)
}

export function fetchConfiguredTenantProvidersInternalApi(tenantId: string) {
  return httpClient.get<ProviderSummary[]>(`${serviceEndpoints.providerService}/v1/internal/getTenantProviders`, {
    params: { tenantId },
  })
}

export function configureTenantProvidersApi(providerIds: string[]) {
  return httpClient.post(`${serviceEndpoints.providerService}/v1/configureTenantProviders`, providerIds)
}

export function updateTenantProviderStatusApi(providerId: string, enabled: boolean, disabledReason?: string) {
  return httpClient.put(`${serviceEndpoints.providerService}/v1/tenantProviders/status`, null, {
    params: {
      providerId,
      enabled,
      disabledReason: disabledReason || undefined,
    },
  })
}

export function removeTenantProviderApi(providerId: string) {
  return httpClient.delete(`${serviceEndpoints.providerService}/v1/tenantProviders`, {
    params: { providerId },
  })
}
