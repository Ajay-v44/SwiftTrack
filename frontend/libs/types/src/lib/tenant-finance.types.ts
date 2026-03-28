export interface AccountResponse {
  id?: string
  balance?: number
  currency?: string
}

export interface TodayExpenseResponse {
  amount: number
}

export interface TenantFinanceSummary {
  balance: number
  weeklySpend: number
  costSavings: number
  unpaidDues: number
  invoiceCount: number
}

export interface LedgerTransactionItem {
  id: string
  description: string
  createdAt: string
  amount: number
  transactionType: string
  referenceType: string
  orderId?: string | null
}

export interface PaginatedLedgerTransactionsResponse {
  items: LedgerTransactionItem[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface TenantFinanceData {
  summary: TenantFinanceSummary
  transactions: PaginatedLedgerTransactionsResponse
}
