export type UserType =
  | "SUPER_ADMIN"
  | "SYSTEM_ADMIN"
  | "SYSTEM_USER"
  | "ADMIN_USER"
  | "TENANT_ADMIN"
  | "TENANT_USER"
  | "TENANT_DRIVER"
  | "TENANT_MANAGER"
  | "TENANT_STAFF"
  | "DRIVER_USER"
  | "CONSUMER"
  | "PROVIDER_USER"
  | "PROVIDER_ADMIN"

export interface UserDetails {
  id: string
  tenantId?: string | null
  providerId?: string | null
  type?: UserType
  name: string
  mobile: string
  roles: string[]
}

export interface LoginResponse {
  accessToken: string
  tokenType?: string
}
