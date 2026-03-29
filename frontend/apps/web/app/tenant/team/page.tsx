"use client"

import type { FormEvent, ReactNode } from "react"
import { useDeferredValue, useEffect, useMemo, useState } from "react"
import { AxiosError } from "axios"
import {
  Building2,
  CheckCircle2,
  Loader2,
  Plus,
  RefreshCw,
  ShieldCheck,
  UserCog,
  UserPlus,
  Users,
  X,
} from "lucide-react"
import { toast } from "sonner"
import { useAuthStore } from "@/store/useAuthStore"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import {
  assignRolesService,
  configureTenantProvidersService,
  createManagedUserService,
  createRoleService,
  fetchActiveDeliveryOptionsService,
  fetchAvailableProvidersService,
  fetchConfiguredTenantProvidersInternalService,
  fetchConfiguredTenantProvidersService,
  fetchRolesService,
  fetchTenantDeliveryConfigurationService,
  fetchTenantUsersService,
  removeTenantProviderService,
  saveTenantDeliveryConfigurationService,
  updateTenantProviderStatusService,
  updateUserStatusService,
} from "@swifttrack/services"
import type {
  DeliveryOptionResponse,
  ProviderSummary,
  RoleViewResponse,
  TenantDeliveryConf,
  TenantProviderConfigSummary,
  TenantTeamUser,
  TenantUserTypeGroup,
  UserType,
} from "@swifttrack/types"

const PAGE_SIZE = 10

const initialCreateUserForm = {
  name: "",
  email: "",
  mobile: "",
  password: "",
  userType: "TENANT_USER" as UserType,
  enabled: true,
}

const initialCreateRoleForm = {
  name: "",
  description: "",
}

export default function TenantTeamPage() {
  const { user } = useAuthStore()
  const [teamUsers, setTeamUsers] = useState<TenantTeamUser[]>([])
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [page, setPage] = useState(0)
  const [searchTerm, setSearchTerm] = useState("")
  const deferredSearch = useDeferredValue(searchTerm)
  const [selectedUserType, setSelectedUserType] = useState<UserType | "ALL">("ALL")
  const [userTypeGroups, setUserTypeGroups] = useState<TenantUserTypeGroup[]>([])
  const [roles, setRoles] = useState<RoleViewResponse[]>([])
  const [loadingUsers, setLoadingUsers] = useState(true)
  const [refreshing, setRefreshing] = useState(false)
  const [createUserOpen, setCreateUserOpen] = useState(false)
  const [createRoleOpen, setCreateRoleOpen] = useState(false)
  const [rolesModalUser, setRolesModalUser] = useState<TenantTeamUser | null>(null)
  const [createUserForm, setCreateUserForm] = useState(initialCreateUserForm)
  const [createUserRoleIds, setCreateUserRoleIds] = useState<string[]>([])
  const [createRoleForm, setCreateRoleForm] = useState(initialCreateRoleForm)
  const [selectedRoleIds, setSelectedRoleIds] = useState<string[]>([])
  const [submittingUser, setSubmittingUser] = useState(false)
  const [submittingRole, setSubmittingRole] = useState(false)
  const [updatingUserId, setUpdatingUserId] = useState<string | null>(null)
  const [savingRoleAssignments, setSavingRoleAssignments] = useState(false)

  const [deliveryOptions, setDeliveryOptions] = useState<DeliveryOptionResponse[]>([])
  const [deliveryConfig, setDeliveryConfig] = useState<Record<string, number>>({})
  const [availableProviders, setAvailableProviders] = useState<ProviderSummary[]>([])
  const [configuredProviders, setConfiguredProviders] = useState<TenantProviderConfigSummary[]>([])
  const [savingDeliveryConfig, setSavingDeliveryConfig] = useState(false)
  const [providerActionId, setProviderActionId] = useState<string | null>(null)
  const [loadingDelivery, setLoadingDelivery] = useState(true)

  const tenantUserGroup = useMemo(
    () => userTypeGroups.find((group) => group.code === "TENANT"),
    [userTypeGroups]
  )

  const selectedDeliveryOptionKeys = useMemo(
    () => Object.keys(deliveryConfig).sort((left, right) => deliveryConfig[left] - deliveryConfig[right]),
    [deliveryConfig]
  )

  const externalProvidersEnabled = selectedDeliveryOptionKeys.includes("EXTERNAL_PROVIDERS")

  useEffect(() => {
    void loadReferenceData()
  }, [])

  useEffect(() => {
    setPage(0)
  }, [deferredSearch, selectedUserType])

  useEffect(() => {
    void loadUsers()
  }, [page, deferredSearch, selectedUserType])

  async function loadReferenceData() {
    setLoadingDelivery(true)

    try {
      const [rolesResponse, userResponse, deliveryOptionsResponse, deliveryConfigResponse, providersResponse, configuredProvidersResponse] =
        await Promise.all([
          fetchRolesService(true),
          fetchTenantUsersService({ page: 0, size: PAGE_SIZE, excludeUserId: user?.id }),
          fetchActiveDeliveryOptionsService(),
          fetchTenantDeliveryConfigurationSafe(),
          fetchAvailableProvidersService(),
          fetchConfiguredTenantProvidersSafe(user?.tenantId || undefined),
        ])

      setRoles(rolesResponse)
      setUserTypeGroups(userResponse.userTypeGroups)
      setDeliveryOptions(deliveryOptionsResponse)
      setDeliveryConfig(toDeliveryConfigMap(deliveryConfigResponse))
      setAvailableProviders(providersResponse)
      setConfiguredProviders(configuredProvidersResponse)
    } catch (error) {
      toast.error(getErrorMessage(error, "Failed to load team management data"))
    } finally {
      setLoadingDelivery(false)
    }
  }

  async function loadUsers(showRefreshState = false) {
    if (showRefreshState) {
      setRefreshing(true)
    } else {
      setLoadingUsers(true)
    }

    try {
      const response = await fetchTenantUsersService({
        page,
        size: PAGE_SIZE,
        query: deferredSearch.trim() || undefined,
        userTypes: selectedUserType === "ALL" ? undefined : [selectedUserType],
        excludeUserId: user?.id,
      })

      setTeamUsers(response.content)
      setTotalPages(response.totalPages)
      setTotalElements(response.totalElements)
      setUserTypeGroups(response.userTypeGroups)
    } catch (error) {
      toast.error(getErrorMessage(error, "Failed to load tenant users"))
    } finally {
      setLoadingUsers(false)
      setRefreshing(false)
    }
  }

  async function handleCreateUser(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmittingUser(true)

    try {
      const createdUser = await createManagedUserService({
        tenantId: user?.tenantId || undefined,
        ...createUserForm,
      })

      if (createUserRoleIds.length > 0) {
        await assignRolesService({
          userId: createdUser.id,
          roleIds: createUserRoleIds,
        })
      }

      toast.success("Tenant user created successfully")
      setCreateUserForm(initialCreateUserForm)
      setCreateUserRoleIds([])
      setCreateUserOpen(false)
      await loadUsers(true)
    } catch (error) {
      toast.error(getErrorMessage(error, "Failed to create tenant user"))
    } finally {
      setSubmittingUser(false)
    }
  }

  async function handleCreateRole(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmittingRole(true)

    try {
      await createRoleService(createRoleForm)
      toast.success("Role created successfully")
      setCreateRoleForm(initialCreateRoleForm)
      setCreateRoleOpen(false)
      setRoles(await fetchRolesService(true))
    } catch (error) {
      toast.error(getErrorMessage(error, "Failed to create role"))
    } finally {
      setSubmittingRole(false)
    }
  }

  async function handleToggleStatus(member: TenantTeamUser) {
    setUpdatingUserId(member.id)

    try {
      await updateUserStatusService({
        userId: member.id,
        status: !member.status,
        verificationStatus: member.verificationStatus,
      })
      toast.success(`${member.name} ${member.status ? "disabled" : "activated"} successfully`)
      await loadUsers(true)
    } catch (error) {
      toast.error(getErrorMessage(error, "Failed to update user status"))
    } finally {
      setUpdatingUserId(null)
    }
  }

  async function handleSaveRoleAssignments() {
    if (!rolesModalUser) {
      return
    }

    setSavingRoleAssignments(true)

    try {
      await assignRolesService({
        userId: rolesModalUser.id,
        roleIds: selectedRoleIds,
      })
      toast.success("Roles assigned successfully")
      setRolesModalUser(null)
      setSelectedRoleIds([])
      await loadUsers(true)
    } catch (error) {
      toast.error(getErrorMessage(error, "Failed to assign roles"))
    } finally {
      setSavingRoleAssignments(false)
    }
  }

  async function handleSaveDeliveryConfiguration() {
    const payload = Object.entries(deliveryConfig)
      .filter(([, priority]) => Number.isFinite(priority))
      .map(([deliveryOption, priority]) => ({
        deliveryOption,
        priority,
      }))
      .sort((left, right) => left.priority - right.priority)

    if (payload.length === 0) {
      toast.error("Select at least one delivery option")
      return
    }

    const priorities = payload.map((item) => item.priority)
    if (new Set(priorities).size !== priorities.length) {
      toast.error("Each selected delivery option must have a unique priority")
      return
    }

    setSavingDeliveryConfig(true)

    try {
      await saveTenantDeliveryConfigurationService(payload)
      toast.success("Delivery configuration saved")
    } catch (error) {
      toast.error(getErrorMessage(error, "Failed to save delivery configuration"))
    } finally {
      setSavingDeliveryConfig(false)
    }
  }

  async function handleAddProvider(providerId: string) {
    setProviderActionId(providerId)
    try {
      await configureTenantProvidersService([providerId])
      toast.success("Provider added for tenant")
      await reloadTenantProviders()
    } catch (error) {
      toast.error(getErrorMessage(error, "Failed to add provider"))
    } finally {
      setProviderActionId(null)
    }
  }

  function toggleDeliveryOption(optionType: string, checked: boolean) {
    setDeliveryConfig((current) => {
      if (checked) {
        const nextPriority = getNextPriority(current)
        return { ...current, [optionType]: nextPriority }
      }

      const next = { ...current }
      delete next[optionType]
      return next
    })
  }

  function updateDeliveryPriority(optionType: string, value: string) {
    const parsedValue = Number(value)
    setDeliveryConfig((current) => ({
      ...current,
      [optionType]: Number.isFinite(parsedValue) && parsedValue > 0 ? parsedValue : 1,
    }))
  }

  function openAssignRoles(member: TenantTeamUser) {
    setRolesModalUser(member)
    setSelectedRoleIds(
      roles
        .filter((role) => member.roles.includes(role.name))
        .map((role) => role.id)
    )
  }

  function toggleRoleSelection(roleId: string, selectedIds: string[], setter: (value: string[]) => void) {
    setter(
      selectedIds.includes(roleId)
        ? selectedIds.filter((id) => id !== roleId)
        : [...selectedIds, roleId]
    )
  }

  async function handleToggleTenantProvider(providerId: string, enabled: boolean) {
    setProviderActionId(providerId)

    try {
      await updateTenantProviderStatusService(
        providerId,
        enabled,
        enabled ? undefined : "Disabled by tenant from team management"
      )
      setConfiguredProviders((current) =>
        current.map((provider) =>
          provider.id === providerId
            ? {
                ...provider,
                enabled,
                disabledReason: enabled ? "" : "Disabled by tenant from team management",
              }
            : provider
        )
      )
      toast.success(`Provider ${enabled ? "enabled" : "disabled"} successfully`)
    } catch (error) {
      toast.error(getErrorMessage(error, "Failed to update tenant provider status"))
    } finally {
      setProviderActionId(null)
    }
  }

  async function handleRemoveProvider(providerId: string) {
    setProviderActionId(providerId)

    try {
      await removeTenantProviderService(providerId)
      toast.success("Provider removed from tenant")
      await reloadTenantProviders()
    } catch (error) {
      toast.error(getErrorMessage(error, "Failed to remove tenant provider"))
    } finally {
      setProviderActionId(null)
    }
  }

  async function reloadTenantProviders() {
    const configured = await fetchConfiguredTenantProvidersSafe(user?.tenantId || undefined)
    setConfiguredProviders(configured)
  }

  const configuredProviderIds = useMemo(
    () => new Set(configuredProviders.map((provider) => provider.id)),
    [configuredProviders]
  )

  const providersAvailableToAdd = useMemo(
    () => availableProviders.filter((provider) => !configuredProviderIds.has(provider.id)),
    [availableProviders, configuredProviderIds]
  )

  return (
    <div className="mx-auto flex w-full max-w-7xl flex-col gap-6 px-4 py-6 sm:px-6 lg:px-8">
      <section className="rounded-[28px] border border-slate-200 bg-[radial-gradient(circle_at_top_left,_rgba(99,102,241,0.14),_transparent_30%),linear-gradient(135deg,#ffffff,#f8fafc)] p-8 shadow-sm">
        <div className="flex flex-col gap-5 lg:flex-row lg:items-end lg:justify-between">
          <div className="space-y-2">
            <Badge variant="outline" className="w-fit rounded-full border-indigo-200 bg-white/80 px-3 py-1 text-indigo-700">
              Team Management
            </Badge>
            <h1 className="text-3xl font-semibold tracking-tight text-slate-950">Tenant users and delivery controls</h1>
            <p className="max-w-2xl text-sm leading-6 text-slate-600">
              Manage tenant-scoped users, assign roles, update status, and configure delivery/provider preferences from one live page.
            </p>
          </div>
          <div className="flex flex-wrap gap-3">
            <Button variant="outline" className="rounded-full border-slate-300 bg-white" onClick={() => setCreateRoleOpen(true)}>
              <ShieldCheck className="h-4 w-4" />
              Create Role
            </Button>
            <Button className="rounded-full bg-slate-950 text-white hover:bg-slate-800" onClick={() => setCreateUserOpen(true)}>
              <UserPlus className="h-4 w-4" />
              Add Tenant User
            </Button>
          </div>
        </div>
      </section>

      <Card className="border-slate-200 bg-white shadow-sm">
        <CardHeader className="border-b border-slate-100 pb-5">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
            <div>
              <CardTitle className="text-slate-950">Tenant Users</CardTitle>
              <CardDescription>
                {totalElements} users found for this tenant. The requesting user is excluded automatically.
              </CardDescription>
            </div>
            <div className="flex flex-col gap-3 sm:flex-row">
              <Input
                value={searchTerm}
                onChange={(event) => setSearchTerm(event.target.value)}
                placeholder="Search by name, email, or mobile"
                className="w-full border-slate-200 bg-slate-50 sm:w-72"
              />
              <select
                value={selectedUserType}
                onChange={(event) => setSelectedUserType(event.target.value as UserType | "ALL")}
                className="h-10 rounded-md border border-slate-200 bg-slate-50 px-3 text-sm text-slate-700"
              >
                <option value="ALL">All tenant user types</option>
                {tenantUserGroup?.userTypes.map((type) => (
                  <option key={type.userType} value={type.userType}>
                    {type.displayName}
                  </option>
                ))}
              </select>
              <Button variant="outline" className="rounded-full border-slate-300 bg-white" onClick={() => void loadUsers(true)}>
                {refreshing ? <Loader2 className="h-4 w-4 animate-spin" /> : <RefreshCw className="h-4 w-4" />}
                Refresh
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent className="pt-6">
          <div className="overflow-hidden rounded-3xl border border-slate-200">
            <div className="overflow-x-auto">
              <table className="min-w-[980px] w-full text-left">
                <thead className="bg-slate-50">
                  <tr>
                    <th className="px-4 py-3 text-xs font-medium uppercase tracking-[0.18em] text-slate-500">User</th>
                    <th className="px-4 py-3 text-xs font-medium uppercase tracking-[0.18em] text-slate-500">Type</th>
                    <th className="px-4 py-3 text-xs font-medium uppercase tracking-[0.18em] text-slate-500">Roles</th>
                    <th className="px-4 py-3 text-xs font-medium uppercase tracking-[0.18em] text-slate-500">Status</th>
                    <th className="px-4 py-3 text-xs font-medium uppercase tracking-[0.18em] text-slate-500">Created</th>
                    <th className="px-4 py-3 text-xs font-medium uppercase tracking-[0.18em] text-slate-500 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-200 bg-white">
                  {loadingUsers ? (
                    Array.from({ length: 5 }, (_, index) => (
                      <tr key={index}>
                        <td className="px-4 py-4" colSpan={6}>
                          <div className="h-12 animate-pulse rounded-2xl bg-slate-100" />
                        </td>
                      </tr>
                    ))
                  ) : teamUsers.length > 0 ? (
                    teamUsers.map((member) => (
                      <tr key={member.id} className="transition hover:bg-slate-50">
                        <td className="px-4 py-4">
                          <div className="flex items-center gap-3">
                            <div className="flex h-10 w-10 items-center justify-center rounded-full bg-slate-100 text-slate-700">
                              <Users className="h-4 w-4" />
                            </div>
                            <div>
                              <p className="text-sm font-medium text-slate-950">{member.name}</p>
                              <p className="text-xs text-slate-500">{member.email}</p>
                              <p className="text-xs text-slate-400">{member.mobile}</p>
                            </div>
                          </div>
                        </td>
                        <td className="px-4 py-4">
                          <span className="inline-flex rounded-full bg-indigo-50 px-3 py-1 text-xs font-medium text-indigo-700">
                            {formatEnumLabel(member.userType)}
                          </span>
                        </td>
                        <td className="px-4 py-4">
                          <div className="flex flex-wrap gap-2">
                            {member.roles.length > 0 ? (
                              member.roles.map((role) => (
                                <span key={role} className="rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-700">
                                  {formatEnumLabel(role)}
                                </span>
                              ))
                            ) : (
                              <span className="text-xs text-slate-400">No roles assigned</span>
                            )}
                          </div>
                        </td>
                        <td className="px-4 py-4">
                          <div className="space-y-2">
                            <span className={`inline-flex rounded-full px-3 py-1 text-xs font-medium ${member.status ? "bg-emerald-50 text-emerald-700" : "bg-rose-50 text-rose-700"}`}>
                              {member.status ? "Active" : "Disabled"}
                            </span>
                            <p className="text-xs text-slate-500">{formatEnumLabel(member.verificationStatus)}</p>
                          </div>
                        </td>
                        <td className="px-4 py-4 text-sm text-slate-500">{formatDate(member.createdAt)}</td>
                        <td className="px-4 py-4">
                          <div className="flex items-center justify-end gap-2">
                            <Button variant="outline" size="sm" className="rounded-full border-slate-300 bg-white" onClick={() => openAssignRoles(member)}>
                              <UserCog className="h-4 w-4" />
                              Roles
                            </Button>
                            <Button
                              variant="outline"
                              size="sm"
                              className="rounded-full border-slate-300 bg-white"
                              onClick={() => void handleToggleStatus(member)}
                              disabled={updatingUserId === member.id}
                            >
                              {updatingUserId === member.id ? <Loader2 className="h-4 w-4 animate-spin" /> : null}
                              {member.status ? "Disable" : "Activate"}
                            </Button>
                          </div>
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td className="px-4 py-10 text-center text-sm text-slate-500" colSpan={6}>
                        No tenant users matched the current filter.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>

          <div className="mt-4 flex items-center justify-between">
            <p className="text-sm text-slate-500">
              Page {totalPages === 0 ? 0 : page + 1} of {totalPages}
            </p>
            <div className="flex gap-2">
              <Button variant="outline" className="rounded-full border-slate-300 bg-white" onClick={() => setPage((current) => Math.max(current - 1, 0))} disabled={page === 0}>
                Previous
              </Button>
              <Button
                variant="outline"
                className="rounded-full border-slate-300 bg-white"
                onClick={() => setPage((current) => current + 1)}
                disabled={page + 1 >= totalPages}
              >
                Next
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      <section className="grid gap-6 xl:grid-cols-[minmax(0,1.1fr)_minmax(360px,0.9fr)]">
        <Card className="border-slate-200 bg-white shadow-sm">
          <CardHeader className="border-b border-slate-100 pb-5">
            <CardTitle className="text-slate-950">Delivery Configuration</CardTitle>
            <CardDescription>
              Loaded from `/tenant-delivery/v1/configure` and `/tenant-delivery/v1/options/active`.
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4 pt-6">
            {loadingDelivery ? (
              <div className="flex items-center gap-3 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-6 text-sm text-slate-500">
                <Loader2 className="h-4 w-4 animate-spin" />
                Loading delivery configuration...
              </div>
            ) : (
              <>
                <div className="grid gap-4">
                  {deliveryOptions.map((option) => {
                    const selected = option.optionType in deliveryConfig

                    return (
                      <div key={option.id} className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                        <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
                          <div className="flex items-start gap-3">
                            <input
                              type="checkbox"
                              checked={selected}
                              onChange={(event) => toggleDeliveryOption(option.optionType, event.target.checked)}
                              className="mt-1 h-4 w-4 rounded border-slate-300"
                            />
                            <div>
                              <p className="text-sm font-medium text-slate-950">{formatEnumLabel(option.optionType)}</p>
                              <p className="text-xs text-slate-500">Active option available in the tenant delivery service.</p>
                            </div>
                          </div>
                          <div className="flex items-center gap-3">
                            <label className="text-xs uppercase tracking-[0.18em] text-slate-500">Priority</label>
                            <Input
                              type="number"
                              min="1"
                              value={selected ? deliveryConfig[option.optionType] : ""}
                              onChange={(event) => updateDeliveryPriority(option.optionType, event.target.value)}
                              disabled={!selected}
                              className="w-24 border-slate-200 bg-white"
                            />
                          </div>
                        </div>
                      </div>
                    )
                  })}
                </div>

                <div className="flex justify-end">
                  <Button className="rounded-full bg-slate-950 text-white hover:bg-slate-800" onClick={() => void handleSaveDeliveryConfiguration()} disabled={savingDeliveryConfig}>
                    {savingDeliveryConfig ? <Loader2 className="h-4 w-4 animate-spin" /> : <CheckCircle2 className="h-4 w-4" />}
                    Save Delivery Config
                  </Button>
                </div>
              </>
            )}
          </CardContent>
        </Card>

        <Card className="border-slate-200 bg-white shadow-sm">
          <CardHeader className="border-b border-slate-100 pb-5">
            <CardTitle className="text-slate-950">External Providers</CardTitle>
            <CardDescription>
              Uses `/api/providers/v1/internal/getTenantProviders`, `/api/providers/v1/tenantProviders`, and `/api/providers/v1/tenantProviders/status`.
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4 pt-6">
            {!externalProvidersEnabled ? (
              <div className="rounded-2xl border border-dashed border-slate-200 bg-slate-50 px-4 py-8 text-sm text-slate-500">
                Enable `EXTERNAL_PROVIDERS` in delivery configuration to manage provider routing.
              </div>
            ) : (
              <>
                <div className="space-y-4">
                  <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                    <p className="text-xs font-medium uppercase tracking-[0.18em] text-slate-500">Available To Add</p>
                    <div className="mt-3 space-y-3">
                      {providersAvailableToAdd.length > 0 ? (
                        providersAvailableToAdd.map((provider) => (
                          <div key={provider.id} className="rounded-2xl bg-white p-4">
                            <div className="flex items-start justify-between gap-3">
                              <div className="flex items-center gap-3">
                                <div className="rounded-2xl bg-slate-50 p-3 text-slate-700 shadow-sm">
                                  <Building2 className="h-5 w-5" />
                                </div>
                                <div>
                                  <p className="text-sm font-medium text-slate-950">{provider.providerName}</p>
                                  <p className="text-xs text-slate-500">{provider.providerCode}</p>
                                </div>
                              </div>
                              <Button
                                size="sm"
                                className="rounded-full bg-slate-950 text-white hover:bg-slate-800"
                                onClick={() => void handleAddProvider(provider.id)}
                                disabled={providerActionId === provider.id}
                              >
                                {providerActionId === provider.id ? <Loader2 className="h-4 w-4 animate-spin" /> : <Plus className="h-4 w-4" />}
                                Add
                              </Button>
                            </div>
                            <p className="mt-3 text-sm leading-6 text-slate-500">{provider.description || "No provider description available."}</p>
                          </div>
                        ))
                      ) : (
                        <p className="text-sm text-slate-500">All available providers are already added for this tenant.</p>
                      )}
                    </div>
                  </div>

                  <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                    <p className="text-xs font-medium uppercase tracking-[0.18em] text-slate-500">Already Added</p>
                    <div className="mt-3 space-y-3">
                      {configuredProviders.length > 0 ? (
                        configuredProviders.map((provider) => {
                          const enabled = provider.enabled

                          return (
                            <div key={provider.id} className="rounded-2xl bg-white p-4">
                              <div className="flex items-start justify-between gap-3">
                                <div className="flex items-center gap-3">
                                  <div className="rounded-2xl bg-slate-50 p-3 text-slate-700 shadow-sm">
                                    <Building2 className="h-5 w-5" />
                                  </div>
                                  <div>
                                    <p className="text-sm font-medium text-slate-950">{provider.providerName}</p>
                                    <p className="text-xs text-slate-500">{provider.providerCode}</p>
                                    <p className="mt-1 text-xs text-slate-400">{enabled ? "Enabled" : "Disabled"}</p>
                                    {!enabled && provider.disabledReason ? (
                                      <p className="mt-1 text-xs text-rose-500">{provider.disabledReason}</p>
                                    ) : null}
                                  </div>
                                </div>
                                <div className="flex flex-wrap justify-end gap-2">
                                  <Button
                                    variant="outline"
                                    size="sm"
                                    className="rounded-full border-slate-300 bg-white"
                                    onClick={() => void handleToggleTenantProvider(provider.id, !enabled)}
                                    disabled={providerActionId === provider.id}
                                  >
                                    {providerActionId === provider.id ? <Loader2 className="h-4 w-4 animate-spin" /> : null}
                                    {enabled ? "Disable" : "Enable"}
                                  </Button>
                                  <Button
                                    variant="outline"
                                    size="sm"
                                    className="rounded-full border-rose-200 bg-white text-rose-600 hover:bg-rose-50 hover:text-rose-700"
                                    onClick={() => void handleRemoveProvider(provider.id)}
                                    disabled={providerActionId === provider.id}
                                  >
                                    Remove
                                  </Button>
                                </div>
                              </div>
                              <p className="mt-3 text-sm leading-6 text-slate-500">{provider.description || "No provider description available."}</p>
                            </div>
                          )
                        })
                      ) : (
                        <p className="text-sm text-slate-500">No providers are configured for this tenant yet.</p>
                      )}
                    </div>
                  </div>
                </div>
              </>
            )}
          </CardContent>
        </Card>
      </section>

      {createUserOpen ? (
        <ModalShell title="Create Tenant User" description="Creates a tenant-scoped user and optionally assigns roles." onClose={() => setCreateUserOpen(false)}>
          <form className="space-y-4" onSubmit={handleCreateUser}>
            <InputField label="Full Name" value={createUserForm.name} onChange={(value) => setCreateUserForm((current) => ({ ...current, name: value }))} />
            <InputField label="Email" value={createUserForm.email} onChange={(value) => setCreateUserForm((current) => ({ ...current, email: value }))} />
            <InputField label="Mobile" value={createUserForm.mobile} onChange={(value) => setCreateUserForm((current) => ({ ...current, mobile: value }))} />
            <InputField label="Password" type="password" value={createUserForm.password} onChange={(value) => setCreateUserForm((current) => ({ ...current, password: value }))} />

            <div className="space-y-2">
              <label className="text-xs font-medium uppercase tracking-[0.18em] text-slate-500">User Type</label>
              <select
                value={createUserForm.userType}
                onChange={(event) => setCreateUserForm((current) => ({ ...current, userType: event.target.value as UserType }))}
                className="h-10 w-full rounded-md border border-slate-200 bg-slate-50 px-3 text-sm text-slate-700"
              >
                {tenantUserGroup?.userTypes.map((type) => (
                  <option key={type.userType} value={type.userType}>
                    {type.displayName}
                  </option>
                ))}
              </select>
            </div>

            <label className="flex items-center gap-3 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-700">
              <input
                type="checkbox"
                checked={createUserForm.enabled}
                onChange={(event) => setCreateUserForm((current) => ({ ...current, enabled: event.target.checked }))}
                className="h-4 w-4 rounded border-slate-300"
              />
              Enable user immediately
            </label>

            <RoleChecklist
              title="Assign Roles"
              roles={roles}
              selectedRoleIds={createUserRoleIds}
              onToggle={(roleId) => toggleRoleSelection(roleId, createUserRoleIds, setCreateUserRoleIds)}
            />

            <div className="flex justify-end gap-2 pt-2">
              <Button type="button" variant="outline" className="rounded-full border-slate-300 bg-white" onClick={() => setCreateUserOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" className="rounded-full bg-slate-950 text-white hover:bg-slate-800" disabled={submittingUser}>
                {submittingUser ? <Loader2 className="h-4 w-4 animate-spin" /> : <UserPlus className="h-4 w-4" />}
                Create User
              </Button>
            </div>
          </form>
        </ModalShell>
      ) : null}

      {createRoleOpen ? (
        <ModalShell title="Create Role" description="Creates a new role through `/user-role/v1`." onClose={() => setCreateRoleOpen(false)}>
          <form className="space-y-4" onSubmit={handleCreateRole}>
            <InputField label="Role Name" value={createRoleForm.name} onChange={(value) => setCreateRoleForm((current) => ({ ...current, name: value }))} />
            <div className="space-y-2">
              <label className="text-xs font-medium uppercase tracking-[0.18em] text-slate-500">Description</label>
              <textarea
                value={createRoleForm.description}
                onChange={(event) => setCreateRoleForm((current) => ({ ...current, description: event.target.value }))}
                className="min-h-28 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-700 outline-none"
                placeholder="Describe what this role can do"
              />
            </div>

            <div className="flex justify-end gap-2 pt-2">
              <Button type="button" variant="outline" className="rounded-full border-slate-300 bg-white" onClick={() => setCreateRoleOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" className="rounded-full bg-slate-950 text-white hover:bg-slate-800" disabled={submittingRole}>
                {submittingRole ? <Loader2 className="h-4 w-4 animate-spin" /> : <ShieldCheck className="h-4 w-4" />}
                Create Role
              </Button>
            </div>
          </form>
        </ModalShell>
      ) : null}

      {rolesModalUser ? (
        <ModalShell
          title={`Assign Roles to ${rolesModalUser.name}`}
          description="Uses `/api/assignRole/v1/add`. Existing role mappings remain untouched; selected new roles are added."
          onClose={() => setRolesModalUser(null)}
        >
          <div className="space-y-4">
            <RoleChecklist
              title="Available Roles"
              roles={roles}
              selectedRoleIds={selectedRoleIds}
              onToggle={(roleId) => toggleRoleSelection(roleId, selectedRoleIds, setSelectedRoleIds)}
            />

            <div className="flex justify-end gap-2 pt-2">
              <Button type="button" variant="outline" className="rounded-full border-slate-300 bg-white" onClick={() => setRolesModalUser(null)}>
                Cancel
              </Button>
              <Button className="rounded-full bg-slate-950 text-white hover:bg-slate-800" onClick={() => void handleSaveRoleAssignments()} disabled={savingRoleAssignments}>
                {savingRoleAssignments ? <Loader2 className="h-4 w-4 animate-spin" /> : <UserCog className="h-4 w-4" />}
                Save Roles
              </Button>
            </div>
          </div>
        </ModalShell>
      ) : null}
    </div>
  )
}

function ModalShell({
  title,
  description,
  children,
  onClose,
}: {
  title: string
  description: string
  children: ReactNode
  onClose: () => void
}) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/40 p-4 backdrop-blur-sm">
      <div className="w-full max-w-2xl rounded-3xl border border-slate-200 bg-white p-6 shadow-xl">
        <div className="flex items-start justify-between gap-4">
          <div>
            <h2 className="text-2xl font-semibold text-slate-950">{title}</h2>
            <p className="mt-1 text-sm text-slate-500">{description}</p>
          </div>
          <button onClick={onClose} className="rounded-full p-2 text-slate-500 transition hover:bg-slate-100 hover:text-slate-950">
            <X className="h-4 w-4" />
          </button>
        </div>
        <div className="mt-6">{children}</div>
      </div>
    </div>
  )
}

function InputField({
  label,
  value,
  onChange,
  type = "text",
}: {
  label: string
  value: string
  onChange: (value: string) => void
  type?: string
}) {
  return (
    <div className="space-y-2">
      <label className="text-xs font-medium uppercase tracking-[0.18em] text-slate-500">{label}</label>
      <Input type={type} value={value} onChange={(event) => onChange(event.target.value)} className="border-slate-200 bg-slate-50" />
    </div>
  )
}

function RoleChecklist({
  title,
  roles,
  selectedRoleIds,
  onToggle,
}: {
  title: string
  roles: RoleViewResponse[]
  selectedRoleIds: string[]
  onToggle: (roleId: string) => void
}) {
  return (
    <div className="space-y-3">
      <label className="text-xs font-medium uppercase tracking-[0.18em] text-slate-500">{title}</label>
      <div className="max-h-64 space-y-2 overflow-y-auto rounded-2xl border border-slate-200 bg-slate-50 p-3">
        {roles.length > 0 ? (
          roles.map((role) => (
            <label key={role.id} className="flex items-start gap-3 rounded-2xl bg-white px-4 py-3">
              <input
                type="checkbox"
                checked={selectedRoleIds.includes(role.id)}
                onChange={() => onToggle(role.id)}
                className="mt-1 h-4 w-4 rounded border-slate-300"
              />
              <div>
                <p className="text-sm font-medium text-slate-950">{formatEnumLabel(role.name)}</p>
                <p className="text-xs text-slate-500">{role.description || "No role description provided."}</p>
              </div>
            </label>
          ))
        ) : (
          <p className="px-2 py-3 text-sm text-slate-500">No roles available yet.</p>
        )}
      </div>
    </div>
  )
}

async function fetchTenantDeliveryConfigurationSafe(): Promise<TenantDeliveryConf[]> {
  try {
    return await fetchTenantDeliveryConfigurationService()
  } catch {
    return []
  }
}

async function fetchConfiguredTenantProvidersSafe(tenantId?: string): Promise<TenantProviderConfigSummary[]> {
  try {
    return await fetchConfiguredTenantProvidersService()
  } catch {
    if (!tenantId) {
      return []
    }
  }

  try {
    if (!tenantId) {
      return []
    }

    return await fetchConfiguredTenantProvidersInternalService(tenantId)
  } catch {
    return []
  }
}

function toDeliveryConfigMap(configuration: TenantDeliveryConf[]) {
  return configuration.reduce<Record<string, number>>((accumulator, item) => {
    accumulator[item.optionType] = item.priority
    return accumulator
  }, {})
}

function getNextPriority(configuration: Record<string, number>) {
  const values = Object.values(configuration)
  return values.length > 0 ? Math.max(...values) + 1 : 1
}

function formatEnumLabel(value: string) {
  return value
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ")
}

function formatDate(value: string) {
  return new Date(value).toLocaleDateString("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
  })
}

function getErrorMessage(error: unknown, fallback: string) {
  if (error instanceof AxiosError) {
    const data = error.response?.data as { message?: string } | undefined
    return data?.message || fallback
  }

  return error instanceof Error ? error.message : fallback
}
