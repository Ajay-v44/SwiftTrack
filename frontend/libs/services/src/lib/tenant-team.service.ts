import {
  assignRolesApi,
  configureTenantProvidersApi,
  createManagedUserApi,
  createRoleApi,
  fetchActiveDeliveryOptionsApi,
  fetchAvailableProvidersApi,
  fetchConfiguredTenantProvidersApi,
  fetchConfiguredTenantProvidersInternalApi,
  fetchLegacyTenantUsersByTypeApi,
  fetchRolesApi,
  fetchTenantDeliveryConfigurationApi,
  fetchTenantUserTypesApi,
  fetchTenantUsersApi,
  removeTenantProviderApi,
  saveTenantDeliveryConfigurationApi,
  updateTenantProviderStatusApi,
  updateUserStatusApi,
} from "@swifttrack/api-client"
import { AxiosError } from "axios"
import type {
  AssignRolesInput,
  CreateManagedUserInput,
  CreateRoleInput,
  PaginatedTenantUsersResponse,
  ProviderSummary,
  TenantTeamUser,
  TenantUserTypeGroup,
  TenantDeliveryPriorityInput,
  TenantProviderConfigSummary,
  TenantUsersQuery,
  UpdateUserStatusVerificationInput,
  UserType,
} from "@swifttrack/types"

export async function fetchTenantUsersService(query: TenantUsersQuery) {
  try {
    const response = await fetchTenantUsersApi(query)
    return response.data
  } catch (error) {
    if (shouldUseLegacyTenantUsersFallback(error)) {
      return fetchTenantUsersLegacyFallback(query)
    }

    throw error
  }
}

export async function fetchTenantUserTypesService() {
  const response = await fetchTenantUserTypesApi()
  return response.data
}

export async function fetchRolesService(status = true) {
  const response = await fetchRolesApi(status)
  return response.data
}

export async function createRoleService(payload: CreateRoleInput) {
  const response = await createRoleApi(payload)
  return response.data
}

export async function createManagedUserService(payload: CreateManagedUserInput) {
  const response = await createManagedUserApi(payload)
  return response.data
}

export async function assignRolesService(payload: AssignRolesInput) {
  const response = await assignRolesApi(payload)
  return response.data
}

export async function updateUserStatusService(payload: UpdateUserStatusVerificationInput) {
  const response = await updateUserStatusApi(payload)
  return response.data
}

export async function fetchActiveDeliveryOptionsService() {
  const response = await fetchActiveDeliveryOptionsApi()
  return response.data
}

export async function fetchTenantDeliveryConfigurationService() {
  const response = await fetchTenantDeliveryConfigurationApi()
  return response.data
}

export async function saveTenantDeliveryConfigurationService(payload: TenantDeliveryPriorityInput[]) {
  const response = await saveTenantDeliveryConfigurationApi(payload)
  return response.data
}

export async function fetchAvailableProvidersService() {
  const response = await fetchAvailableProvidersApi()
  return response.data
}

export async function fetchConfiguredTenantProvidersService() {
  const response = await fetchConfiguredTenantProvidersApi()
  return response.data.map(normalizeTenantProviderSummary)
}

export async function fetchConfiguredTenantProvidersInternalService(tenantId: string) {
  const response = await fetchConfiguredTenantProvidersInternalApi(tenantId)
  return response.data.map(normalizeTenantProviderSummary)
}

export async function configureTenantProvidersService(providerIds: string[]) {
  const response = await configureTenantProvidersApi(providerIds)
  return response.data
}

export async function updateTenantProviderStatusService(
  providerId: string,
  enabled: boolean,
  disabledReason?: string
) {
  const response = await updateTenantProviderStatusApi(providerId, enabled, disabledReason)
  return response.data
}

export async function removeTenantProviderService(providerId: string) {
  const response = await removeTenantProviderApi(providerId)
  return response.data
}

const LEGACY_TENANT_USER_TYPES: UserType[] = [
  "TENANT_ADMIN",
  "TENANT_MANAGER",
  "TENANT_USER",
  "TENANT_STAFF",
  "TENANT_DRIVER",
]

function shouldUseLegacyTenantUsersFallback(error: unknown) {
  if (!(error instanceof AxiosError)) {
    return false
  }

  const responseMessage =
    typeof (error.response?.data as { message?: unknown } | undefined)?.message === "string"
      ? String((error.response?.data as { message?: string }).message).toLowerCase()
      : ""
  const message = `${error.message.toLowerCase()} ${responseMessage}`

  return (
    message.includes("required request parameter 'usertype'") ||
    message.includes("request method 'get' is not supported") ||
    message.includes("method not supported")
  )
}

async function fetchTenantUsersLegacyFallback(query: TenantUsersQuery): Promise<PaginatedTenantUsersResponse> {
  const requestedTypes =
    query.userTypes && query.userTypes.length > 0 ? query.userTypes : LEGACY_TENANT_USER_TYPES

  const responses = await Promise.all(requestedTypes.map((userType) => fetchLegacyTenantUsersByTypeApi(userType)))

  const allUsers = responses
    .flatMap((response) => response.data)
    .map(normalizeLegacyTenantUser)
    .filter((user, index, array) => array.findIndex((candidate) => candidate.id === user.id) === index)
    .filter((user) => (query.includeRequestingUser ? true : user.id !== query.excludeUserId))
    .filter((user) => {
      const rawQuery = query.query?.trim().toLowerCase()
      if (!rawQuery) {
        return true
      }

      return (
        user.name.toLowerCase().includes(rawQuery) ||
        user.email.toLowerCase().includes(rawQuery) ||
        user.mobile.toLowerCase().includes(rawQuery)
      )
    })

  const page = query.page ?? 0
  const size = query.size ?? 10
  const startIndex = page * size
  const paginatedUsers = allUsers.slice(startIndex, startIndex + size)

  return {
    content: paginatedUsers,
    page,
    size,
    totalElements: allUsers.length,
    totalPages: allUsers.length === 0 ? 0 : Math.ceil(allUsers.length / size),
    userTypeGroups: buildStaticTenantUserTypeGroups(),
  }
}

function normalizeLegacyTenantUser(user: {
  id: string
  name: string
  mobile: string
  email: string
  status: boolean
  userType: UserType
}): TenantTeamUser {
  const timestamp = new Date(0).toISOString()

  return {
    id: user.id,
    name: user.name,
    mobile: user.mobile,
    email: user.email,
    status: user.status,
    verificationStatus: user.status ? "APPROVED" : "PENDING",
    userType: user.userType,
    roles: [],
    createdAt: timestamp,
    updatedAt: timestamp,
  }
}

function buildStaticTenantUserTypeGroups(): TenantUserTypeGroup[] {
  return [
    {
      code: "TENANT",
      displayName: "Tenant Users",
      description: "Tenant-scoped workspace roles used in the tenant dashboard.",
      userTypes: [
        { userType: "TENANT_ADMIN", displayName: "Tenant Admin", description: "Tenant owner and workspace admin" },
        { userType: "TENANT_MANAGER", displayName: "Tenant Manager", description: "Operations manager within a tenant" },
        { userType: "TENANT_USER", displayName: "Tenant User", description: "General tenant workspace user" },
        { userType: "TENANT_STAFF", displayName: "Tenant Staff", description: "Tenant staff member" },
        { userType: "TENANT_DRIVER", displayName: "Tenant Driver", description: "Driver employed by the tenant" },
      ],
    },
  ]
}

function normalizeTenantProviderSummary(provider: ProviderSummary): TenantProviderConfigSummary {
  const timestamp = new Date(0).toISOString()

  return {
    ...provider,
    enabled: true,
    verified: false,
    disabledReason: "",
    createdAt: timestamp,
    updatedAt: timestamp,
  }
}
