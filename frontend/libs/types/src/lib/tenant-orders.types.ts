export interface TenantOrderQuoteFormInput {
  pickupAddressId: string
  dropoffLat: number
  dropoffLng: number
}

export interface TenantOrderQuote {
  quoteSessionId: string
  quoteId: string | null
  selectedType: string | null
  providerCode: string | null
  price: number
  currency: string | null
}

export interface CustomerDeliveryOptionQuote {
  quoteOptionId: string
  choiceCode: string | null
  selectedType: string | null
  providerCode: string | null
  price: number
  currency: string | null
  providerQuoteId: string | null
}

export interface CustomerDeliveryOptionsQuote {
  quoteSessionId: string
  options: CustomerDeliveryOptionQuote[]
}

export interface TenantSavedAddress {
  id: string
  label: string | null
  line1: string
  line2: string | null
  city: string
  state: string
  country: string
  pincode: string
  locality: string | null
  latitude: number | null
  longitude: number | null
  contactName: string
  contactPhone: string
  businessName: string | null
  notes: string | null
  isDefault: boolean
}

export interface TenantSavedAddressInput {
  label?: string | null
  line1: string
  line2?: string | null
  city: string
  state: string
  country: string
  pincode: string
  locality?: string | null
  latitude: number
  longitude: number
  contactName: string
  contactPhone: string
  businessName?: string | null
  notes?: string | null
  isDefault?: boolean
}

export interface TenantPlaceSuggestion {
  placeId: string | null
  displayName: string | null
  formattedAddress: string | null
  city: string | null
  state: string | null
  country: string | null
  countryCode: string | null
  postalCode: string | null
  locality: string | null
  latitude: number
  longitude: number
}

export interface TenantDropoffLocationDraft {
  addressId?: string | null
  label?: string | null
  line1: string
  line2?: string | null
  city: string
  state: string
  country: string
  pincode: string
  locality?: string | null
  latitude: number
  longitude: number
  contactName: string
  contactPhone: string
  businessName?: string | null
  notes?: string | null
}

export interface TenantCreateOrderInput {
  quoteSessionId: string
  orderReference: string
  paymentType: "PREPAID" | "COD"
  pickupAddressId: string
  dropoff: TenantDropoffLocationDraft
  packageInfo: {
    totalValue?: number | null
    totalWeightGrams: number
    size: "SMALL" | "MEDIUM" | "LARGE" | "XL"
    description: string
  }
  deliveryInstructions?: string | null
}

export interface CustomerCreateOrderInput extends TenantCreateOrderInput {
  selectedQuoteId: string
}

export interface TenantOrdersFilterInput {
  query?: string
  page: number
  size: number
  startDate?: string
  endDate?: string
}

export interface TenantOrderListItem {
  id: string
  customerReferenceId: string | null
  orderStatus: string
  pickupCity: string | null
  dropoffCity: string | null
  operator: string | null
  createdAt: string
}

export interface TenantOrdersSummary {
  processedOrders: number
  openIssues: number
  deliveredOrders: number
  activeOrders: number
}

export interface PaginatedTenantOrdersResponse {
  items: TenantOrderListItem[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  summary: TenantOrdersSummary
}

export interface TenantOrderLocationInfo {
  id: string
  type: string | null
  latitude: number | null
  longitude: number | null
  line1: string | null
  line2: string | null
  city: string | null
  state: string | null
  country: string | null
  pincode: string | null
  locality: string | null
  createdAt: string | null
}

export interface TenantOrderCurrentLocationInfo {
  status: string | null
  latitude: number | null
  longitude: number | null
  updatedAt: string | null
}

export interface TenantOrderDebitInfo {
  accountId: string
  orderId: string
  debitedAmount: number | null
  lastDebitedAt: string | null
  description: string | null
}

export interface TenantOrderTimelineEvent {
  id: string
  providerCode: string | null
  status: string | null
  latitude: number | null
  longitude: number | null
  description: string | null
  eventTime: string | null
  createdAt: string | null
}

export interface TenantOrderDetailsResponse {
  id: string
  tenantId: string | null
  ownerUserId: string | null
  createdBy: string | null
  assignedDriverId: string | null
  accessScope: string | null
  customerReferenceId: string | null
  orderStatus: string
  trackingStatus: string | null
  bookingChannel: string | null
  orderType: string | null
  paymentType: string | null
  paymentAmount: number | null
  selectedProviderCode: string | null
  providerOrderId: string | null
  quoteSessionId: string | null
  selectedType: string | null
  city: string | null
  state: string | null
  pickupLat: number | null
  pickupLng: number | null
  dropoffLat: number | null
  dropoffLng: number | null
  createdAt: string | null
  updatedAt: string | null
  lastStatusUpdatedAt: string | null
  lastLocationUpdatedAt: string | null
  pickup: TenantOrderLocationInfo | null
  dropoff: TenantOrderLocationInfo | null
  currentLocation: TenantOrderCurrentLocationInfo | null
  tenantDebit: TenantOrderDebitInfo | null
  locations: TenantOrderLocationInfo[]
}

export interface TenantOrderTrackingResponse {
  orderId: string
  orderStatus: string | null
  trackingStatus: string | null
  lastStatusUpdatedAt: string | null
  lastLocationUpdatedAt: string | null
  currentLocation: TenantOrderCurrentLocationInfo | null
  pickup: TenantOrderLocationInfo | null
  dropoff: TenantOrderLocationInfo | null
  locations: TenantOrderLocationInfo[]
  trackingHistory: TenantOrderTimelineEvent[]
}
