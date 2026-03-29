import {
  fetchTenantOrderDetailsApi,
  fetchTenantOrderTrackingApi,
  fetchTenantOrdersApi,
  searchTenantOrdersApi,
} from "@swifttrack/api-client"
import {
  PaginatedTenantOrdersResponse,
  TenantOrderDetailsResponse,
  TenantOrderListItem,
  TenantOrderQuote,
  TenantOrderQuoteFormInput,
  TenantOrderTrackingResponse,
  TenantOrdersFilterInput,
} from "@swifttrack/types"

function wait(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

export async function fetchTenantOrderQuotesService(
  _input: TenantOrderQuoteFormInput
): Promise<TenantOrderQuote[]> {
  await wait(1500)

  return [
    { id: "Q-1001", provider: "DHL Express", price: 145, eta: "2 Days", tag: "Fastest" },
    { id: "Q-1002", provider: "FedEx Freight", price: 120, eta: "3 Days", tag: "Best Value" },
    { id: "Q-1003", provider: "Internal Fleet", price: 95, eta: "5 Days", tag: "Lowest Cost" },
  ]
}

export async function dispatchTenantOrderService(_quoteId: string): Promise<void> {
  await wait(1500)
}

export async function fetchTenantOrdersService(
  filters: TenantOrdersFilterInput
): Promise<PaginatedTenantOrdersResponse> {
  const response = filters.query?.trim()
    ? await searchTenantOrdersApi({
        ...filters,
        query: filters.query.trim(),
      })
    : await fetchTenantOrdersApi(filters)

  return response.data
}

export async function fetchTenantOrderDetailsService(orderId: string): Promise<TenantOrderDetailsResponse> {
  const response = await fetchTenantOrderDetailsApi(orderId)
  return normalizeTenantOrderDetailsResponse(response.data)
}

export async function fetchTenantOrderTrackingService(orderId: string): Promise<TenantOrderTrackingResponse> {
  const response = await fetchTenantOrderTrackingApi(orderId)
  return response.data
}

export function buildFallbackTenantOrderDetails(order: TenantOrderListItem): TenantOrderDetailsResponse {
  return normalizeTenantOrderDetailsResponse({
    id: order.id,
    customerReferenceId: order.customerReferenceId,
    orderStatus: order.orderStatus,
    trackingStatus: order.orderStatus,
    selectedProviderCode: order.operator,
    city: order.pickupCity,
    state: null,
    createdAt: order.createdAt,
    updatedAt: order.createdAt,
    locations: [
      {
        id: `${order.id}-pickup`,
        type: "PICKUP",
        latitude: null,
        longitude: null,
        city: order.pickupCity,
        state: null,
        country: null,
        pincode: null,
        locality: null,
        createdAt: order.createdAt,
      },
      {
        id: `${order.id}-drop`,
        type: "DROP",
        latitude: null,
        longitude: null,
        city: order.dropoffCity,
        state: null,
        country: null,
        pincode: null,
        locality: null,
        createdAt: order.createdAt,
      },
    ],
  })
}

function normalizeTenantOrderDetailsResponse(
  raw: Partial<TenantOrderDetailsResponse> & {
    id: string
    customerReferenceId?: string | null
    orderStatus?: string | null
    city?: string | null
    state?: string | null
    pickupLat?: number | null
    pickupLng?: number | null
    dropoffLat?: number | null
    dropoffLng?: number | null
  }
): TenantOrderDetailsResponse {
  const pickup =
    raw.pickup ??
    (raw.pickupLat != null || raw.pickupLng != null || raw.city
      ? {
          id: `${raw.id}-pickup`,
          type: "PICKUP",
          latitude: raw.pickupLat ?? null,
          longitude: raw.pickupLng ?? null,
          city: raw.city ?? null,
          state: raw.state ?? null,
          country: null,
          pincode: null,
          locality: null,
          createdAt: raw.createdAt ?? null,
        }
      : null)

  const dropoff =
    raw.dropoff ??
    (raw.dropoffLat != null || raw.dropoffLng != null
      ? {
          id: `${raw.id}-drop`,
          type: "DROP",
          latitude: raw.dropoffLat ?? null,
          longitude: raw.dropoffLng ?? null,
          city: null,
          state: null,
          country: null,
          pincode: null,
          locality: null,
          createdAt: raw.createdAt ?? null,
        }
      : null)

  return {
    id: raw.id,
    tenantId: raw.tenantId ?? null,
    ownerUserId: raw.ownerUserId ?? null,
    createdBy: raw.createdBy ?? null,
    assignedDriverId: raw.assignedDriverId ?? null,
    accessScope: raw.accessScope ?? null,
    customerReferenceId: raw.customerReferenceId ?? null,
    orderStatus: raw.orderStatus ?? "UNKNOWN",
    trackingStatus: raw.trackingStatus ?? raw.orderStatus ?? "UNKNOWN",
    bookingChannel: raw.bookingChannel ?? null,
    orderType: raw.orderType ?? null,
    paymentType: raw.paymentType ?? null,
    paymentAmount: raw.paymentAmount ?? null,
    selectedProviderCode: raw.selectedProviderCode ?? null,
    providerOrderId: raw.providerOrderId ?? null,
    quoteSessionId: raw.quoteSessionId ?? null,
    selectedType: raw.selectedType ?? null,
    city: raw.city ?? pickup?.city ?? null,
    state: raw.state ?? pickup?.state ?? null,
    pickupLat: raw.pickupLat ?? pickup?.latitude ?? null,
    pickupLng: raw.pickupLng ?? pickup?.longitude ?? null,
    dropoffLat: raw.dropoffLat ?? dropoff?.latitude ?? null,
    dropoffLng: raw.dropoffLng ?? dropoff?.longitude ?? null,
    createdAt: raw.createdAt ?? null,
    updatedAt: raw.updatedAt ?? raw.createdAt ?? null,
    lastStatusUpdatedAt: raw.lastStatusUpdatedAt ?? raw.updatedAt ?? raw.createdAt ?? null,
    lastLocationUpdatedAt: raw.lastLocationUpdatedAt ?? null,
    pickup,
    dropoff,
    currentLocation: raw.currentLocation ?? null,
    tenantDebit: raw.tenantDebit ?? null,
    locations: raw.locations ?? [pickup, dropoff].filter((item): item is NonNullable<typeof item> => Boolean(item)),
  }
}
