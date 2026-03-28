import { TenantOrderQuote, TenantOrderQuoteFormInput } from "@swifttrack/types"

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
