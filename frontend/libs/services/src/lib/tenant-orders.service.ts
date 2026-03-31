import {
  cancelTenantOrderApi,
  createTenantAddressApi,
  createTenantOrderApi,
  fetchPlaceSuggestionsApi,
  fetchTenantAddressesApi,
  fetchTenantOrderDetailsApi,
  fetchTenantOrderQuoteApi,
  fetchTenantOrderTrackingApi,
  fetchTenantOrdersApi,
  searchTenantOrdersApi,
  setTenantDefaultAddressApi,
  type RawTenantCreateOrderResponse,
  type RawTenantOrderQuoteResponse,
  updateTenantAddressApi,
  type RawMapPlaceSuggestion,
} from "@swifttrack/api-client"
import {
  PaginatedTenantOrdersResponse,
  TenantCreateOrderInput,
  TenantOrderDetailsResponse,
  TenantOrderListItem,
  TenantOrderQuote,
  TenantOrderQuoteFormInput,
  TenantOrderTrackingResponse,
  TenantOrdersFilterInput,
  TenantPlaceSuggestion,
  TenantSavedAddress,
  TenantSavedAddressInput,
} from "@swifttrack/types"

export async function fetchTenantOrderQuotesService(
  input: TenantOrderQuoteFormInput
): Promise<TenantOrderQuote> {
  const response = await fetchTenantOrderQuoteApi(input)
  return mapTenantQuoteResponse(response.data)
}

export async function createTenantOrderService(input: TenantCreateOrderInput): Promise<{ orderId: string }> {
  const response = await createTenantOrderApi(input)
  return mapTenantCreateOrderResponse(response.data)
}

export async function fetchTenantAddressesService(): Promise<TenantSavedAddress[]> {
  const response = await fetchTenantAddressesApi()
  return response.data
}

export async function createTenantAddressService(input: TenantSavedAddressInput): Promise<TenantSavedAddress> {
  const response = await createTenantAddressApi(input)
  return response.data
}

export async function updateTenantAddressService(
  addressId: string,
  input: TenantSavedAddressInput
): Promise<TenantSavedAddress> {
  const response = await updateTenantAddressApi(addressId, input)
  return response.data
}

export async function setTenantDefaultAddressService(addressId: string): Promise<TenantSavedAddress> {
  const response = await setTenantDefaultAddressApi(addressId)
  return response.data
}

export async function fetchPlaceSuggestionsService(query: string, limit = 5): Promise<TenantPlaceSuggestion[]> {
  const response = await fetchPlaceSuggestionsApi(query, limit)
  return (response.data.data ?? [])
    .map(mapPlaceSuggestion)
    .filter((item): item is TenantPlaceSuggestion => item !== null)
}

export async function cancelTenantOrderService(orderId: string): Promise<void> {
  await cancelTenantOrderApi(orderId)
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

function mapPlaceSuggestion(raw: RawMapPlaceSuggestion): TenantPlaceSuggestion | null {
  const latitude = raw.coordinates?.lat
  const longitude = raw.coordinates?.lng

  if (typeof latitude !== "number" || typeof longitude !== "number") {
    return null
  }

  return {
    placeId: raw.place_id,
    displayName: raw.display_name,
    formattedAddress: raw.formatted_address,
    city: raw.city,
    state: raw.state,
    country: raw.country,
    countryCode: raw.country_code,
    postalCode: raw.postal_code,
    locality: raw.locality,
    latitude,
    longitude,
  }
}

function mapTenantQuoteResponse(raw: RawTenantOrderQuoteResponse): TenantOrderQuote {
  return {
    quoteSessionId: raw.quoteSessionId,
    quoteId: raw.quoteId ?? raw.quoteResponse?.quoteId ?? null,
    selectedType: raw.selectedType ?? null,
    providerCode: raw.providerCode ?? null,
    price: typeof raw.quoteResponse?.price === "number" ? raw.quoteResponse.price : 0,
    currency: raw.quoteResponse?.currency ?? null,
  }
}

function mapTenantCreateOrderResponse(raw: RawTenantCreateOrderResponse): { orderId: string } {
  return {
    orderId: raw.orderId,
  }
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
