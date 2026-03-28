export interface TenantOrderQuoteFormInput {
  originAddress: string
  originCity: string
  originZip: string
  destAddress: string
  destCity: string
  destZip: string
  weight: string
  dimensions: string
  type: string
}

export interface TenantOrderQuote {
  id: string
  provider: string
  price: number
  eta: string
  tag: string
}
