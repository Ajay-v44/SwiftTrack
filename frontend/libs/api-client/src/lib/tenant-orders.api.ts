import { httpClient } from "./http-client"
import { serviceEndpoints } from "./endpoints"
import type {
  TenantCreateOrderInput,
  TenantOrderQuote,
  TenantOrderQuoteFormInput,
  TenantSavedAddress,
  TenantSavedAddressInput,
} from "@swifttrack/types"

export interface MapApiResponse<T> {
  success: boolean
  data: T
}

export interface RawMapPlaceSuggestion {
  place_id: string | null
  display_name: string | null
  formatted_address: string | null
  city: string | null
  state: string | null
  country: string | null
  country_code: string | null
  postal_code: string | null
  locality: string | null
  street: string | null
  house_number: string | null
  coordinates?: {
    lat?: number | null
    lng?: number | null
  } | null
}

export function fetchTenantAddressesApi() {
  return httpClient.get<TenantSavedAddress[]>(`${serviceEndpoints.orders}/addresses/v1`)
}

export function createTenantAddressApi(payload: TenantSavedAddressInput) {
  return httpClient.post<TenantSavedAddress>(`${serviceEndpoints.orders}/addresses/v1`, payload)
}

export function updateTenantAddressApi(addressId: string, payload: TenantSavedAddressInput) {
  return httpClient.put<TenantSavedAddress>(`${serviceEndpoints.orders}/addresses/v1/${addressId}`, payload)
}

export function setTenantDefaultAddressApi(addressId: string) {
  return httpClient.post<TenantSavedAddress>(`${serviceEndpoints.orders}/addresses/v1/${addressId}/default`)
}

export function fetchPlaceSuggestionsApi(query: string, limit = 5) {
  return httpClient.get<MapApiResponse<RawMapPlaceSuggestion[]>>(`${serviceEndpoints.mapService}/map/search`, {
    params: { query, limit },
  })
}

export function fetchTenantOrderQuoteApi(payload: TenantOrderQuoteFormInput) {
  return httpClient.post<TenantOrderQuote>(`${serviceEndpoints.orders}/v1/getQuote`, payload)
}

export function createTenantOrderApi(payload: TenantCreateOrderInput) {
  const { quoteSessionId, ...body } = payload

  return httpClient.post<{ orderId: string }>(`${serviceEndpoints.orders}/v1/createOrder`, body, {
    params: { quoteSessionId },
  })
}
