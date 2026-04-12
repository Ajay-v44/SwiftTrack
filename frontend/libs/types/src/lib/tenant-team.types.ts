import type { UserType } from "./auth.types"

export interface TenantUserTypeOption {
  userType: UserType
  displayName: string
  description: string
}

export interface TenantUserTypeGroup {
  code: string
  displayName: string
  description: string
  userTypes: TenantUserTypeOption[]
}

export interface TenantTeamUser {
  id: string
  name: string
  mobile: string
  email: string
  status: boolean
  verificationStatus: "PENDING" | "APPROVED" | "REJECTED"
  userType: UserType
  roles: string[]
  createdAt: string
  updatedAt: string
}

export interface PaginatedTenantUsersResponse {
  content: TenantTeamUser[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  userTypeGroups: TenantUserTypeGroup[]
}

export interface TenantUsersQuery {
  page?: number
  size?: number
  query?: string
  userTypes?: UserType[]
  includeRequestingUser?: boolean
  excludeUserId?: string
}

export interface RoleViewResponse {
  id: string
  name: string
  description: string
  status: boolean
}

export interface CreateManagedUserInput {
  tenantId?: string | null
  name: string
  password: string
  email: string
  mobile: string
  userType: UserType
  enabled?: boolean
}

export interface AssignRolesInput {
  userId: string
  roleIds: string[]
}

export interface UpdateUserStatusVerificationInput {
  userId: string
  status: boolean
  verificationStatus: "PENDING" | "APPROVED" | "REJECTED"
}

export interface CreateRoleInput {
  name: string
  description: string
}

export interface DeliveryOptionResponse {
  id: string
  optionType: string
  active: boolean
}

export interface TenantDeliveryConf {
  optionType: string
  priority: number
}

export interface TenantDeliveryPriorityInput {
  deliveryOption: string
  priority: number
}

export interface ProviderSummary {
  id: string
  providerName: string
  providerCode: string
  description: string
  logoUrl: string
  websiteUrl: string
  supportsHyperlocal: boolean
  supportsCourier: boolean
  supportsSameDay: boolean
  supportsIntercity: boolean
  servicableAreas: string[]
}

export interface TenantProviderConfigSummary extends ProviderSummary {
  enabled: boolean
  verified: boolean
  disabledReason: string
  createdAt: string
  updatedAt: string
}

export interface ProviderOnboardingRequestInput {
  providerName: string
  contactPhone: string
  contactEmail: string
  notes: string
  docLinks: string
  providerWebsite: string
}
