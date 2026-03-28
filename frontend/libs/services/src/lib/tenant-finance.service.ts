import {
  fetchTenantFinanceSummaryApi,
  fetchTenantTransactionsApi,
} from "@swifttrack/api-client"
import { TenantFinanceData } from "@swifttrack/types"

export async function fetchTenantFinanceService(page: number, size: number): Promise<TenantFinanceData> {
  const [summaryResponse, transactionsResponse] = await Promise.all([
    fetchTenantFinanceSummaryApi(),
    fetchTenantTransactionsApi(page, size),
  ])

  return {
    summary: summaryResponse.data,
    transactions: transactionsResponse.data,
  }
}
