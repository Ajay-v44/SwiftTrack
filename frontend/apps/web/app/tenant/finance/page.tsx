"use client"

import {
  ArrowRightLeft,
  ChevronLeft,
  ChevronRight,
  CreditCard,
  Download,
  FileSpreadsheet,
  Filter,
  Radio,
  Target,
  TrendingUp,
  Wallet,
} from "lucide-react"
import { useTenantFinance } from "@/hooks/useTenantFinance"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"

const currencyFormatter = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
})

const dateFormatter = new Intl.DateTimeFormat("en-IN", {
  day: "2-digit",
  month: "short",
  year: "numeric",
})

export default function TenantFinancePage() {
  const { summary, transactions, page, setPage, loading, error } = useTenantFinance()

  const pageNumbers = Array.from({ length: transactions.totalPages }, (_, index) => index)
  const showPagination = transactions.totalPages > 1

  const topStats = [
    {
      title: "Weekly Spend",
      value: currencyFormatter.format(summary.weeklySpend),
      icon: TrendingUp,
      accent: "bg-indigo-50 text-indigo-700",
    },
    {
      title: "Cost Savings",
      value: currencyFormatter.format(summary.costSavings),
      icon: Wallet,
      accent: "bg-emerald-50 text-emerald-700",
    },
    {
      title: "Unpaid Dues",
      value: currencyFormatter.format(summary.unpaidDues),
      icon: ArrowRightLeft,
      accent: "bg-rose-50 text-rose-700",
    },
    {
      title: "Invoices",
      value: `${summary.invoiceCount} Total`,
      icon: FileSpreadsheet,
      accent: "bg-sky-50 text-sky-700",
    },
  ]

  return (
    <div className="mx-auto flex w-full max-w-7xl flex-col gap-6 px-4 py-6 sm:px-6 lg:px-8">
      {error ? (
        <div className="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">{error}</div>
      ) : null}

      <section className="grid gap-6 xl:grid-cols-[minmax(0,1.45fr)_minmax(320px,0.9fr)]">
        <Card className="overflow-hidden border-slate-200 bg-[radial-gradient(circle_at_top_left,_rgba(99,102,241,0.14),_transparent_30%),linear-gradient(135deg,#ffffff,#f8fafc)] shadow-sm">
          <CardContent className="flex flex-col gap-8 px-6 py-8 sm:px-8">
            <div className="flex flex-col gap-5 sm:flex-row sm:items-start sm:justify-between">
              <div className="space-y-3">
                <Badge variant="outline" className="rounded-full border-indigo-200 bg-white/80 px-3 py-1 text-indigo-700">
                  Finance Hub
                </Badge>
                <div className="space-y-2">
                  <p className="text-xs font-medium uppercase tracking-[0.24em] text-slate-500">Available Credits</p>
                  {loading ? (
                    <StatSkeleton className="h-14 w-52" />
                  ) : (
                    <div className="text-4xl font-semibold tracking-tight text-slate-950 sm:text-5xl">
                      {currencyFormatter.format(summary.balance)}
                    </div>
                  )}
                  <p className="max-w-lg text-sm leading-6 text-slate-600">
                    Monitor wallet health, invoice volume, and transaction flow without leaving the tenant workspace.
                  </p>
                </div>
              </div>

              <div className="inline-flex w-fit items-center gap-2 rounded-full border border-emerald-200 bg-emerald-50 px-4 py-2 text-xs font-semibold uppercase tracking-[0.18em] text-emerald-700">
                <Radio className="h-3.5 w-3.5" />
                Live updates
              </div>
            </div>

            <div className="flex flex-wrap gap-3">
              <Button className="rounded-full bg-slate-950 text-white hover:bg-slate-800">
                <CreditCard className="h-4 w-4" />
                Top Up Wallet
              </Button>
              <Button variant="outline" className="rounded-full border-slate-300 bg-white">
                Transfer Funds
              </Button>
            </div>
          </CardContent>
        </Card>

        <Card className="border-slate-200 bg-white shadow-sm">
          <CardHeader className="border-b border-slate-100 pb-5">
            <div className="flex items-center gap-3">
              <div className="rounded-2xl bg-slate-100 p-3 text-slate-700">
                <Target className="h-5 w-5" />
              </div>
              <div>
                <CardTitle className="text-slate-950">Reconciliation</CardTitle>
                <CardDescription>Review statements and auto-match progress.</CardDescription>
              </div>
            </div>
          </CardHeader>
          <CardContent className="space-y-5 pt-6">
            <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
              <div className="flex items-center justify-between gap-4">
                <div>
                  <p className="text-sm font-medium text-slate-950">Current Statement</p>
                  <p className="mt-1 text-sm text-slate-500">Latest invoice pack available for review.</p>
                </div>
                <span className="rounded-full bg-emerald-50 px-3 py-1 text-xs font-medium text-emerald-700">Ready</span>
              </div>
            </div>

            <div className="space-y-3">
              <FinanceMeta label="Pending Invoices" value="02" />
              <FinanceMeta label="Auto-reconcile" value="Enabled" accent="text-emerald-700" />
            </div>

            <div className="space-y-2">
              <div className="h-2 overflow-hidden rounded-full bg-slate-100">
                <div className="h-full w-[85%] rounded-full bg-slate-950" />
              </div>
              <p className="text-xs text-slate-500">85% of transactions are auto-matched.</p>
            </div>

            <Button variant="outline" className="w-full rounded-full border-slate-300 bg-white">
              View Full History
            </Button>
          </CardContent>
        </Card>
      </section>

      <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {topStats.map((item) => (
          <Card key={item.title} className="border-slate-200 bg-white shadow-sm">
            <CardContent className="flex items-start gap-4 px-5 py-5">
              <div className={`rounded-2xl p-3 ${item.accent}`}>
                <item.icon className="h-5 w-5" />
              </div>
              <div className="min-w-0 space-y-2">
                <p className="text-xs font-medium uppercase tracking-[0.2em] text-slate-500">{item.title}</p>
                {loading ? (
                  <StatSkeleton className="h-8 w-28" />
                ) : (
                  <div className="break-words text-2xl font-semibold text-slate-950">{item.value}</div>
                )}
              </div>
            </CardContent>
          </Card>
        ))}
      </section>

      <Card className="border-slate-200 bg-white shadow-sm">
        <CardHeader className="gap-4 border-b border-slate-100 pb-5">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
            <div>
              <CardTitle className="text-slate-950">Transaction History</CardTitle>
              <CardDescription>Detailed breakdown of financial activity for the tenant account.</CardDescription>
            </div>
            <div className="flex flex-wrap gap-2">
              <Button variant="outline" size="sm" className="rounded-full border-slate-300 bg-white">
                <Filter className="h-4 w-4" />
                Filter
              </Button>
              <Button variant="outline" size="sm" className="rounded-full border-slate-300 bg-white">
                <Download className="h-4 w-4" />
                Export
              </Button>
            </div>
          </div>
        </CardHeader>

        <CardContent className="pt-6">
          {loading ? (
            <div className="space-y-3">
              {Array.from({ length: 5 }, (_, index) => (
                <div key={index} className="grid gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4 md:grid-cols-5">
                  <StatSkeleton className="h-5 w-24" />
                  <StatSkeleton className="h-5 w-full" />
                  <StatSkeleton className="h-5 w-28" />
                  <StatSkeleton className="h-5 w-24" />
                  <StatSkeleton className="h-5 w-20" />
                </div>
              ))}
            </div>
          ) : (
            <div className="overflow-hidden rounded-3xl border border-slate-200">
              <div className="overflow-x-auto">
                <table className="min-w-[760px] w-full text-left">
                  <thead className="bg-slate-50">
                    <tr>
                      <th className="px-4 py-3 text-xs font-medium uppercase tracking-[0.18em] text-slate-500">Transaction ID</th>
                      <th className="px-4 py-3 text-xs font-medium uppercase tracking-[0.18em] text-slate-500">Description</th>
                      <th className="px-4 py-3 text-xs font-medium uppercase tracking-[0.18em] text-slate-500">Date</th>
                      <th className="px-4 py-3 text-xs font-medium uppercase tracking-[0.18em] text-slate-500">Amount</th>
                      <th className="px-4 py-3 text-xs font-medium uppercase tracking-[0.18em] text-slate-500">Type</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-200 bg-white">
                    {transactions.items.map((txn) => (
                      <tr key={txn.id} className="align-top transition hover:bg-slate-50">
                        <td className="px-4 py-4 text-sm font-medium text-slate-700">#{txn.id.slice(0, 8).toUpperCase()}</td>
                        <td className="px-4 py-4">
                          <div className="flex items-center gap-3">
                            <div className="rounded-xl bg-slate-100 p-2 text-slate-700">
                              <FileSpreadsheet className="h-4 w-4" />
                            </div>
                            <span className="text-sm font-medium text-slate-950">{txn.description}</span>
                          </div>
                        </td>
                        <td className="px-4 py-4 text-sm text-slate-600">{dateFormatter.format(new Date(txn.createdAt))}</td>
                        <td className="px-4 py-4 text-sm font-semibold">
                          <span className={txn.amount > 0 ? "text-emerald-700" : "text-rose-700"}>
                            {txn.amount > 0 ? "+ " : "- "}
                            {currencyFormatter.format(Math.abs(txn.amount))}
                          </span>
                        </td>
                        <td className="px-4 py-4">
                          <span
                            className={`inline-flex rounded-full px-3 py-1 text-xs font-medium ${
                              txn.amount > 0 ? "bg-emerald-50 text-emerald-700" : "bg-rose-50 text-rose-700"
                            }`}
                          >
                            {txn.transactionType}
                          </span>
                        </td>
                      </tr>
                    ))}
                    {transactions.items.length === 0 ? (
                      <tr>
                        <td colSpan={5} className="px-4 py-10 text-center text-sm text-slate-500">
                          No transaction history found.
                        </td>
                      </tr>
                    ) : null}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          <div className="mt-5 flex flex-col gap-3 text-sm text-slate-500 sm:flex-row sm:items-center sm:justify-between">
            <span>
              {loading
                ? "Loading transactions..."
                : `Showing ${transactions.items.length} of ${transactions.totalElements} transactions`}
            </span>
            <div className="flex flex-wrap items-center gap-2">
              <button
                className="inline-flex h-9 w-9 items-center justify-center rounded-full border border-slate-200 bg-white text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40"
                disabled={loading || page === 0}
                onClick={() => setPage((currentPage) => Math.max(currentPage - 1, 0))}
              >
                <ChevronLeft className="h-4 w-4" />
              </button>
              {showPagination
                ? pageNumbers.map((pageNumber) => (
                    <button
                      key={pageNumber}
                      className={`inline-flex h-9 w-9 items-center justify-center rounded-full border text-sm font-medium transition ${
                        pageNumber === page
                          ? "border-slate-950 bg-slate-950 text-white"
                          : "border-slate-200 bg-white text-slate-700 hover:bg-slate-50"
                      }`}
                      disabled={loading}
                      onClick={() => setPage(pageNumber)}
                    >
                      {pageNumber + 1}
                    </button>
                  ))
                : null}
              <button
                className="inline-flex h-9 w-9 items-center justify-center rounded-full border border-slate-200 bg-white text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40"
                disabled={loading || page >= transactions.totalPages - 1 || transactions.totalPages === 0}
                onClick={() => setPage((currentPage) => Math.min(currentPage + 1, transactions.totalPages - 1))}
              >
                <ChevronRight className="h-4 w-4" />
              </button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}

function FinanceMeta({
  label,
  value,
  accent,
}: {
  label: string
  value: string
  accent?: string
}) {
  return (
    <div className="flex items-center justify-between text-sm">
      <span className="text-slate-500">{label}</span>
      <span className={`font-medium text-slate-950 ${accent || ""}`.trim()}>{value}</span>
    </div>
  )
}

function StatSkeleton({ className }: { className?: string }) {
  return <div className={`animate-pulse rounded-xl bg-slate-200 ${className || ""}`.trim()} />
}
