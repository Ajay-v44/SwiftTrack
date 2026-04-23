import { httpClient } from "./http-client"
import { serviceEndpoints } from "./endpoints"
import type {
  TenantCreateOrderInput,
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

export interface RawTenantOrderQuoteResponse {
  quoteSessionId: string
  selectedType: string | null
  providerCode: string | null
  quoteId: string | null
  quoteResponse?: {
    price?: number | null
    currency?: string | null
    quoteId?: string | null
  } | null
}

export interface RawTenantCreateOrderResponse {
  orderId: string
  providerCode?: string | null
  totalAmount?: number | null
  choiceCode?: string | null
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
  return httpClient.post<RawTenantOrderQuoteResponse>(`${serviceEndpoints.orders}/v1/getQuote`, payload)
}

export function createTenantOrderApi(payload: TenantCreateOrderInput) {
  const { quoteSessionId, orderReference, paymentType, pickupAddressId, dropoff, packageInfo, deliveryInstructions } =
    payload

  const body = {
    orderReference,
    orderType: "ON_DEMAND",
    paymentType,
    pickupAddressId,
    dropoff: {
      addressId: dropoff.addressId ?? null,
      address: {
        line1: dropoff.line1,
        line2: dropoff.line2 ?? null,
        city: dropoff.city,
        state: dropoff.state,
        country: dropoff.country,
        pincode: dropoff.pincode,
        locality: dropoff.locality ?? null,
        latitude: dropoff.latitude,
        longitude: dropoff.longitude,
      },
      contact: {
        name: dropoff.contactName,
        phone: dropoff.contactPhone,
      },
      businessName: dropoff.businessName ?? null,
      notes: dropoff.notes ?? null,
      verification: null,
    },
    packageInfo,
    deliveryInstructions,
  }

  return httpClient.post<RawTenantCreateOrderResponse>(`${serviceEndpoints.orders}/v1/createOrder`, body, {
    params: { quoteSessionId },
  })
}

export function cancelTenantOrderApi(orderId: string) {
  return httpClient.post<{ message?: string }>(`${serviceEndpoints.orders}/v1/cancelOrder`, null, {
    params: { orderId },
  })
}

export function fetchPublicOrderTrackingApi(trackingId: string) {
  return httpClient.get<any>(`${serviceEndpoints.orders}/v1/public/track/${trackingId}`)
}
