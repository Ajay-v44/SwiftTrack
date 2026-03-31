"use client"

import { Suspense, useEffect, useMemo, useState } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import { Building2, CheckCircle2, Loader2, Network, Route, ShieldAlert } from "lucide-react"
import { toast } from "sonner"
import {
  configureTenantProvidersService,
  fetchActiveDeliveryOptionsService,
  fetchAvailableProvidersService,
  fetchConfiguredTenantProvidersService,
  fetchTenantDeliveryConfigurationService,
  fetchTenantSetupStatusService,
  registerTenantCompanyService,
  saveTenantDeliveryConfigurationService,
} from "@swifttrack/services"
import type {
  CompanyRegistrationInput,
  DeliveryOptionResponse,
  ProviderSummary,
  TenantDeliveryPriorityInput,
  TenantSetupStatus,
} from "@swifttrack/types"
import { useAuthStore } from "@/store/useAuthStore"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"

type SetupStep = "company" | "providers" | "delivery"

const DEFAULT_COMPANY_FORM: CompanyRegistrationInput = {
  legalName: "",
  registrationNumber: "",
  incorporationDate: "",
  industryVertical: "Third-Party Logistics (3PL)",
}

const STEP_ORDER: SetupStep[] = ["company", "providers", "delivery"]

export default function TenantSetupPage() {
  return (
    <Suspense fallback={
      <div className="flex min-h-[60vh] items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-slate-500" />
      </div>
    }>
      <TenantSetupPageContent />
    </Suspense>
  )
}

function TenantSetupPageContent() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const { user } = useAuthStore()

  const [setupStatus, setSetupStatus] = useState<TenantSetupStatus | null>(null)
  const [loadingStatus, setLoadingStatus] = useState(true)
  const [companySaving, setCompanySaving] = useState(false)
  const [providerSaving, setProviderSaving] = useState(false)
  const [deliverySaving, setDeliverySaving] = useState(false)

  const [companyForm, setCompanyForm] = useState<CompanyRegistrationInput>(DEFAULT_COMPANY_FORM)
  const [providers, setProviders] = useState<ProviderSummary[]>([])
  const [selectedProviderIds, setSelectedProviderIds] = useState<string[]>([])
  const [deliveryOptions, setDeliveryOptions] = useState<DeliveryOptionResponse[]>([])
  const [deliveryOrder, setDeliveryOrder] = useState<string[]>([])

  const requestedStep = searchParams.get("step")
  const currentStep = useMemo<SetupStep>(() => {
    const fallbackStep =
      setupStatus?.nextStep === "providers" || setupStatus?.nextStep === "delivery" ? setupStatus.nextStep : "company"

    if (requestedStep === "providers" || requestedStep === "delivery" || requestedStep === "company") {
      const requestedIndex = STEP_ORDER.indexOf(requestedStep)
      const fallbackIndex = STEP_ORDER.indexOf(fallbackStep)
      return requestedIndex > fallbackIndex ? fallbackStep : requestedStep
    }

    return fallbackStep
  }, [requestedStep, setupStatus?.nextStep])

  useEffect(() => {
    void bootstrap()
  }, [])

  async function bootstrap() {
    setLoadingStatus(true)
    try {
      const status = await fetchTenantSetupStatusService()
      setSetupStatus(status)

      if (status.setupComplete) {
        router.replace("/tenant/dashboard")
        return
      }

      await Promise.all([loadProviders(), loadDeliveryOptions()])
    } catch (error) {
      console.error(error)
      toast.error("Failed to load tenant setup state")
    } finally {
      setLoadingStatus(false)
    }
  }

  async function loadProviders() {
    const [availableProviders, configuredProviders] = await Promise.all([
      fetchAvailableProvidersService(),
      fetchConfiguredTenantProvidersService().catch(() => []),
    ])

    setProviders(availableProviders)
    setSelectedProviderIds(configuredProviders.map((provider) => provider.id))
  }

  async function loadDeliveryOptions() {
    const [options, configured] = await Promise.all([
      fetchActiveDeliveryOptionsService(),
      fetchTenantDeliveryConfigurationService().catch(() => []),
    ])

    setDeliveryOptions(options)
    if (configured.length > 0) {
      const configuredOrder = configured
        .slice()
        .sort((left, right) => left.priority - right.priority)
        .map((item) => item.optionType)
      setDeliveryOrder(configuredOrder)
      return
    }

    setDeliveryOrder(options.map((option) => option.optionType))
  }

  function updateStep(step: SetupStep) {
    router.replace(`/tenant/setup?step=${step}`)
  }

  async function refreshStatus(nextStepFallback: SetupStep) {
    const status = await fetchTenantSetupStatusService()
    setSetupStatus(status)

    if (status.setupComplete) {
      toast.success("Tenant setup completed")
      router.replace("/tenant/dashboard")
      return
    }

    updateStep((status.nextStep === "complete" ? nextStepFallback : status.nextStep) as SetupStep)
  }

  async function handleCompanySubmit(event: React.FormEvent) {
    event.preventDefault()
    if (!user?.id) {
      toast.error("Please log in again")
      return
    }

    setCompanySaving(true)
    try {
      await registerTenantCompanyService(user.id, companyForm)
      toast.success("Company details saved")
      await refreshStatus("providers")
    } catch (error) {
      console.error(error)
      toast.error("Failed to save company details")
    } finally {
      setCompanySaving(false)
    }
  }

  async function handleProvidersSubmit() {
    if (selectedProviderIds.length === 0) {
      toast.error("Select at least one external provider")
      return
    }

    setProviderSaving(true)
    try {
      await configureTenantProvidersService(selectedProviderIds)
      toast.success("External providers configured")
      await refreshStatus("delivery")
    } catch (error) {
      console.error(error)
      toast.error("Failed to configure providers")
    } finally {
      setProviderSaving(false)
    }
  }

  async function handleDeliverySubmit() {
    if (deliveryOrder.length === 0) {
      toast.error("Choose at least one delivery preference")
      return
    }

    const payload: TenantDeliveryPriorityInput[] = deliveryOrder.map((deliveryOption, index) => ({
      deliveryOption,
      priority: index + 1,
    }))

    setDeliverySaving(true)
    try {
      await saveTenantDeliveryConfigurationService(payload)
      toast.success("Delivery preferences saved")
      await refreshStatus("delivery")
    } catch (error) {
      console.error(error)
      toast.error("Failed to save delivery preferences")
    } finally {
      setDeliverySaving(false)
    }
  }

  function toggleProvider(providerId: string) {
    setSelectedProviderIds((current) =>
      current.includes(providerId) ? current.filter((id) => id !== providerId) : [...current, providerId]
    )
  }

  function moveDeliveryOption(optionType: string, direction: "up" | "down") {
    setDeliveryOrder((current) => {
      const index = current.indexOf(optionType)
      if (index === -1) {
        return current
      }

      const targetIndex = direction === "up" ? index - 1 : index + 1
      if (targetIndex < 0 || targetIndex >= current.length) {
        return current
      }

      const next = [...current]
      const [item] = next.splice(index, 1)
      next.splice(targetIndex, 0, item)
      return next
    })
  }

  function toggleDeliveryOption(optionType: string) {
    setDeliveryOrder((current) =>
      current.includes(optionType) ? current.filter((item) => item !== optionType) : [...current, optionType]
    )
  }

  const stepState = {
    company: setupStatus?.companyRegistered ?? false,
    providers: setupStatus?.providersConfigured ?? false,
    delivery: setupStatus?.deliveryPreferencesConfigured ?? false,
  }

  if (loadingStatus) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-slate-500" />
      </div>
    )
  }

  return (
    <div className="mx-auto flex w-full max-w-6xl flex-col gap-6 px-4 py-6 sm:px-6 lg:px-8">
      <Card className="border-slate-200 bg-white shadow-sm">
        <CardHeader className="border-b border-slate-100">
          <CardTitle className="text-2xl text-slate-950">Complete Tenant Onboarding</CardTitle>
          <CardDescription>
            Registration creates the tenant admin account. Finish company setup, provider configuration, and delivery
            preferences before creating orders.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-6 pt-6">
          <div className="grid gap-3 md:grid-cols-3">
            <StepCard
              title="Company"
              description="Register your tenant organization"
              icon={Building2}
              active={currentStep === "company"}
              complete={stepState.company}
            />
            <StepCard
              title="Providers"
              description="Enable external delivery providers"
              icon={Network}
              active={currentStep === "providers"}
              complete={stepState.providers}
            />
            <StepCard
              title="Delivery"
              description="Set delivery preference priority"
              icon={Route}
              active={currentStep === "delivery"}
              complete={stepState.delivery}
            />
          </div>

          {currentStep === "company" ? (
            <form onSubmit={handleCompanySubmit}>
              <Card className="border-slate-200 shadow-none">
                <CardHeader>
                  <CardTitle className="text-lg">Company Registration</CardTitle>
                  <CardDescription>This creates the tenant organization record tied to your account.</CardDescription>
                </CardHeader>
                <CardContent className="grid gap-4 md:grid-cols-2">
                  <Field
                    id="legalName"
                    label="Legal Company Name"
                    value={companyForm.legalName}
                    onChange={(value) => setCompanyForm((current) => ({ ...current, legalName: value }))}
                  />
                  <Field
                    id="registrationNumber"
                    label="Registration Number"
                    value={companyForm.registrationNumber}
                    onChange={(value) => setCompanyForm((current) => ({ ...current, registrationNumber: value }))}
                  />
                  <Field
                    id="incorporationDate"
                    label="Incorporation Date"
                    type="date"
                    value={companyForm.incorporationDate}
                    onChange={(value) => setCompanyForm((current) => ({ ...current, incorporationDate: value }))}
                  />
                  <div className="space-y-2">
                    <Label htmlFor="industryVertical">Industry Vertical</Label>
                    <select
                      id="industryVertical"
                      value={companyForm.industryVertical}
                      onChange={(event) =>
                        setCompanyForm((current) => ({ ...current, industryVertical: event.target.value }))
                      }
                      className="flex h-10 w-full rounded-md border border-slate-200 bg-white px-3 py-2 text-sm"
                    >
                      <option value="Third-Party Logistics (3PL)">Third-Party Logistics (3PL)</option>
                      <option value="Freight Forwarding">Freight Forwarding</option>
                      <option value="E-commerce Fulfillment">E-commerce Fulfillment</option>
                      <option value="Enterprise Fleet">Enterprise Fleet</option>
                    </select>
                  </div>
                  <div className="md:col-span-2">
                    <Button type="submit" disabled={companySaving}>
                      {companySaving ? <Loader2 className="h-4 w-4 animate-spin" /> : null}
                      Save Company Details
                    </Button>
                  </div>
                </CardContent>
              </Card>
            </form>
          ) : null}

          {currentStep === "providers" ? (
            <Card className="border-slate-200 shadow-none">
              <CardHeader>
                <CardTitle className="text-lg">Configure External Providers</CardTitle>
                <CardDescription>Select at least one provider to enable order routing.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid gap-3 md:grid-cols-2">
                  {providers.map((provider) => {
                    const selected = selectedProviderIds.includes(provider.id)
                    return (
                      <button
                        key={provider.id}
                        type="button"
                        onClick={() => toggleProvider(provider.id)}
                        className={`rounded-2xl border p-4 text-left transition ${
                          selected
                            ? "border-slate-900 bg-slate-900 text-white"
                            : "border-slate-200 bg-white text-slate-900 hover:border-slate-300"
                        }`}
                      >
                        <div className="flex items-start justify-between gap-3">
                          <div>
                            <p className="font-semibold">{provider.providerName}</p>
                            <p className={`mt-1 text-sm ${selected ? "text-slate-200" : "text-slate-500"}`}>
                              {provider.description || provider.providerCode}
                            </p>
                          </div>
                          {selected ? <CheckCircle2 className="h-5 w-5" /> : null}
                        </div>
                      </button>
                    )
                  })}
                </div>
                <Button onClick={handleProvidersSubmit} disabled={providerSaving}>
                  {providerSaving ? <Loader2 className="h-4 w-4 animate-spin" /> : null}
                  Save Providers
                </Button>
              </CardContent>
            </Card>
          ) : null}

          {currentStep === "delivery" ? (
            <Card className="border-slate-200 shadow-none">
              <CardHeader>
                <CardTitle className="text-lg">Delivery Preferences</CardTitle>
                <CardDescription>Order the delivery options by priority for quote and order flows.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-3">
                {deliveryOptions.length === 0 ? (
                  <div className="rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">
                    No active delivery options are available yet.
                  </div>
                ) : (
                  deliveryOptions.map((option) => {
                    const enabled = deliveryOrder.includes(option.optionType)
                    const priority = enabled ? deliveryOrder.indexOf(option.optionType) + 1 : null
                    return (
                      <div key={option.id} className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-slate-200 p-4">
                        <div>
                          <p className="font-medium text-slate-950">{option.optionType}</p>
                          <p className="text-sm text-slate-500">
                            {enabled ? `Priority ${priority}` : "Disabled for tenant routing"}
                          </p>
                        </div>
                        <div className="flex flex-wrap items-center gap-2">
                          <Button type="button" variant="outline" onClick={() => toggleDeliveryOption(option.optionType)}>
                            {enabled ? "Disable" : "Enable"}
                          </Button>
                          <Button
                            type="button"
                            variant="outline"
                            disabled={!enabled}
                            onClick={() => moveDeliveryOption(option.optionType, "up")}
                          >
                            Move Up
                          </Button>
                          <Button
                            type="button"
                            variant="outline"
                            disabled={!enabled}
                            onClick={() => moveDeliveryOption(option.optionType, "down")}
                          >
                            Move Down
                          </Button>
                        </div>
                      </div>
                    )
                  })
                )}
                <Button onClick={handleDeliverySubmit} disabled={deliverySaving || deliveryOrder.length === 0}>
                  {deliverySaving ? <Loader2 className="h-4 w-4 animate-spin" /> : null}
                  Save Delivery Preferences
                </Button>
              </CardContent>
            </Card>
          ) : null}

          <div className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-600">
            <div className="flex items-start gap-2">
              <ShieldAlert className="mt-0.5 h-4 w-4 text-slate-500" />
              Your progress is stored on the server. If you leave, log out, or come back later, login resumes from the
              next incomplete setup step automatically.
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}

function StepCard({
  title,
  description,
  icon: Icon,
  active,
  complete,
}: {
  title: string
  description: string
  icon: typeof Building2
  active: boolean
  complete: boolean
}) {
  return (
    <div className={`rounded-3xl border p-4 ${active ? "border-slate-900 bg-slate-900 text-white" : "border-slate-200 bg-white"} `}>
      <div className="flex items-center gap-3">
        <div className={`rounded-2xl p-3 ${active ? "bg-white/10" : "bg-slate-100 text-slate-700"}`}>
          <Icon className="h-5 w-5" />
        </div>
        <div>
          <p className="font-semibold">{title}</p>
          <p className={`text-sm ${active ? "text-slate-200" : "text-slate-500"}`}>{description}</p>
        </div>
      </div>
      {complete ? <p className={`mt-3 text-xs font-medium ${active ? "text-emerald-300" : "text-emerald-600"}`}>Completed</p> : null}
    </div>
  )
}

function Field({
  id,
  label,
  type = "text",
  value,
  onChange,
}: {
  id: string
  label: string
  type?: string
  value: string
  onChange: (value: string) => void
}) {
  return (
    <div className="space-y-2">
      <Label htmlFor={id}>{label}</Label>
      <Input id={id} type={type} value={value} onChange={(event) => onChange(event.target.value)} required />
    </div>
  )
}
