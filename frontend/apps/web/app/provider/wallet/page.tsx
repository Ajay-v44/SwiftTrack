"use client";

import React, { useState, useEffect } from 'react';
import {
  Wallet, ArrowDownRight, ArrowUpRight, Clock, FileText,
  Search, Landmark, Edit2, History, CheckCircle, XCircle, AlertCircle
} from 'lucide-react';
import {
  getAccountSummaryApi,
  getMyAccountApi,
  getTransactionsApi,
  getBankDetailsApi,
  saveBankDetailsApi,
  initiateSettlementApi,
  getSettlementsByAccountApi
} from '@swifttrack/api-client';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";

type BankDetails = {
  accountNumber: string;
  ifscCode: string;
  upiId: string;
  accountHolderName: string;
  bankName: string;
};

const defaultBankDetails: BankDetails = {
  accountNumber: '',
  ifscCode: '',
  upiId: '',
  accountHolderName: '',
  bankName: '',
};

export default function ProviderWallet() {
  const [loading, setLoading] = useState(true);
  const [balance, setBalance] = useState<number>(0);
  const [accountId, setAccountId] = useState<string | null>(null);
  const [transactions, setTransactions] = useState<any[]>([]);
  const [settlementLogs, setSettlementLogs] = useState<any[]>([]);

  // Bank details state
  const [bankDetails, setBankDetails] = useState<BankDetails>(defaultBankDetails);
  const [hasBank, setHasBank] = useState(false);
  const [isEditingBank, setIsEditingBank] = useState(false);
  const [saving, setSaving] = useState(false);

  // Withdraw state
  const [amount, setAmount] = useState('');
  const [withdrawing, setWithdrawing] = useState(false);
  const [toast, setToast] = useState<{ type: 'success' | 'error'; msg: string } | null>(null);

  // Search filter for transactions
  const [search, setSearch] = useState('');

  useEffect(() => {
    fetchWalletData();
  }, []);

  const showToast = (type: 'success' | 'error', msg: string) => {
    setToast({ type, msg });
    setTimeout(() => setToast(null), 4000);
  };

  const fetchWalletData = async () => {
    try {
      setLoading(true);
      const [summaryRes, accountRes, txRes, bankRes] = await Promise.allSettled([
        getAccountSummaryApi(),
        getMyAccountApi(),
        getTransactionsApi(0, 100),
        getBankDetailsApi(),
      ]);

      if (summaryRes.status === 'fulfilled' && summaryRes.value?.data) {
        setBalance(Number(summaryRes.value.data.balance) || 0);
      }

      let resolvedAccountId: string | null = null;
      if (accountRes.status === 'fulfilled' && accountRes.value?.data?.id) {
        resolvedAccountId = accountRes.value.data.id as string;
        setAccountId(resolvedAccountId);
        fetchSettlementLogs(resolvedAccountId);
      }

      if (txRes.status === 'fulfilled' && txRes.value?.data?.items) {
        setTransactions(txRes.value.data.items);
      }

      if (bankRes.status === 'fulfilled' && bankRes.value?.data) {
        const d = bankRes.value.data;
        setBankDetails({
          accountNumber: d.accountNumber || '',
          ifscCode: d.ifscCode || '',
          upiId: d.upiId || '',
          accountHolderName: d.accountHolderName || '',
          bankName: d.bankName || '',
        });
        const hasBankData = !!(d.accountNumber || d.upiId);
        setHasBank(hasBankData);
        setIsEditingBank(!hasBankData);
      } else {
        // 404 means no bank details yet
        setIsEditingBank(true);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const fetchSettlementLogs = async (id: string) => {
    try {
      const res = await getSettlementsByAccountApi(id);
      const sorted = ((res.data as any[]) || []).sort(
        (a: any, b: any) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      );
      setSettlementLogs(sorted);
    } catch (e) {
      console.error(e);
    }
  };

  const handleSaveBankDetails = async () => {
    if (!bankDetails.accountNumber && !bankDetails.upiId) {
      showToast('error', 'Please provide at least an Account Number or UPI ID.');
      return;
    }
    try {
      setSaving(true);
      await saveBankDetailsApi(bankDetails);
      setHasBank(true);
      setIsEditingBank(false);
      showToast('success', 'Bank details saved successfully.');
    } catch (e: any) {
      showToast('error', e?.response?.data?.message || 'Failed to save bank details.');
    } finally {
      setSaving(false);
    }
  };

  const handleWithdraw = async () => {
    const numAmount = Number(amount);
    if (!amount || isNaN(numAmount) || numAmount <= 0) {
      showToast('error', 'Please enter a valid amount.');
      return;
    }
    if (numAmount > balance) {
      showToast('error', 'Amount exceeds your available balance.');
      return;
    }
    if (!accountId) {
      showToast('error', 'Account ID missing. Please refresh and try again.');
      return;
    }
    if (!hasBank) {
      showToast('error', 'Please add your bank details first.');
      setIsEditingBank(true);
      return;
    }

    try {
      setWithdrawing(true);
      await initiateSettlementApi(accountId, numAmount);
      showToast('success', 'Withdrawal initiated successfully!');
      setAmount('');
      // Refresh data
      const summaryRes = await getAccountSummaryApi();
      if (summaryRes?.data) setBalance(Number(summaryRes.data.balance) || 0);
      fetchSettlementLogs(accountId);
    } catch (e: any) {
      showToast('error', e?.response?.data?.message || 'Failed to initiate withdrawal.');
    } finally {
      setWithdrawing(false);
    }
  };

  const filteredTx = transactions.filter(tx =>
    !search ||
    (tx.description || '').toLowerCase().includes(search.toLowerCase()) ||
    (tx.referenceType || '').toLowerCase().includes(search.toLowerCase())
  );

  const statusIcon = (status: string) => {
    if (status === 'SETTLED') return <CheckCircle className="w-4 h-4 text-emerald-500" />;
    if (status === 'FAILED') return <XCircle className="w-4 h-4 text-rose-500" />;
    return <AlertCircle className="w-4 h-4 text-amber-500" />;
  };

  const statusColor = (status: string) => {
    if (status === 'SETTLED') return 'bg-emerald-50 dark:bg-emerald-500/10 text-emerald-700 dark:text-emerald-400';
    if (status === 'FAILED') return 'bg-rose-50 dark:bg-rose-500/10 text-rose-700 dark:text-rose-400';
    return 'bg-amber-50 dark:bg-amber-500/10 text-amber-700 dark:text-amber-400';
  };

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center p-12 text-slate-400">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="p-8 md:p-12 space-y-8 min-h-screen bg-slate-50 dark:bg-slate-950 relative">

      {/* Toast */}
      {toast && (
        <div className={`fixed top-6 right-6 z-50 flex items-center gap-3 px-5 py-4 rounded-2xl shadow-xl text-sm font-semibold transition-all ${
          toast.type === 'success'
            ? 'bg-emerald-600 text-white'
            : 'bg-rose-600 text-white'
        }`}>
          {toast.type === 'success' ? <CheckCircle className="w-5 h-5" /> : <XCircle className="w-5 h-5" />}
          {toast.msg}
        </div>
      )}

      <h1 className="text-3xl font-bold text-slate-900 dark:text-white tracking-tight">Provider Wallet</h1>

      <div className="grid gap-6 lg:grid-cols-[minmax(0,1.6fr)_minmax(320px,1fr)]">

        {/* Left column */}
        <div className="space-y-6">

          {/* Balance card */}
          <Card className="overflow-hidden border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-xl relative rounded-[2rem]">
            <div className="absolute right-0 top-0 w-72 h-72 bg-blue-500/10 rounded-full blur-[80px] pointer-events-none" />
            <CardContent className="px-8 py-10 relative z-10">
              <p className="text-sm font-medium uppercase tracking-widest text-slate-500 dark:text-slate-400 mb-2">Available Balance</p>
              <div className="text-5xl font-bold tracking-tight text-slate-900 dark:text-white mb-6">
                ₹{balance.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
              </div>

              {/* Withdraw amount input */}
              {!isEditingBank && (
                <div className="space-y-4">
                  <p className="text-sm font-semibold text-slate-700 dark:text-slate-300">Withdraw to Bank</p>
                  <div className="flex gap-3">
                    <div className="flex items-center flex-1 bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-2xl px-5 focus-within:ring-2 focus-within:ring-blue-500 transition-all">
                      <span className="text-2xl font-semibold text-slate-400 mr-2">₹</span>
                      <input
                        type="number"
                        min={0}
                        max={balance}
                        placeholder="0.00"
                        value={amount}
                        onChange={e => setAmount(e.target.value)}
                        className="flex-1 bg-transparent text-2xl font-bold text-slate-900 dark:text-white py-4 outline-none placeholder:text-slate-300 dark:placeholder:text-slate-600"
                      />
                    </div>
                    <Button
                      onClick={handleWithdraw}
                      disabled={withdrawing}
                      className="rounded-2xl bg-blue-600 hover:bg-blue-700 text-white font-bold px-8 py-6 text-base shadow-lg shadow-blue-500/20 transition-all hover:scale-[1.02] active:scale-95 disabled:opacity-60"
                    >
                      {withdrawing ? 'Processing...' : 'Withdraw'}
                    </Button>
                  </div>
                </div>
              )}

              {isEditingBank && !hasBank && (
                <p className="text-sm text-amber-600 dark:text-amber-400 bg-amber-50 dark:bg-amber-500/10 px-4 py-3 rounded-xl">
                  Add your bank details below to enable withdrawals.
                </p>
              )}
            </CardContent>
          </Card>

          {/* Settlement history */}
          <Card className="border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-xl rounded-[2rem] overflow-hidden">
            <CardHeader className="border-b border-slate-100 dark:border-slate-800/50 pb-5">
              <div className="flex items-center gap-3">
                <div className="rounded-xl bg-slate-100 dark:bg-slate-800 p-2.5">
                  <History className="h-5 w-5 text-slate-600 dark:text-slate-300" />
                </div>
                <div>
                  <CardTitle className="text-slate-900 dark:text-white text-lg">Withdrawal History</CardTitle>
                  <CardDescription className="text-slate-500">Recent settlement requests</CardDescription>
                </div>
              </div>
            </CardHeader>
            <CardContent className="p-0">
              {settlementLogs.length === 0 ? (
                <div className="p-10 text-center text-slate-500 dark:text-slate-400 text-sm">
                  No withdrawals yet.
                </div>
              ) : (
                <div className="divide-y divide-slate-100 dark:divide-slate-800">
                  {settlementLogs.slice(0, 10).map((log: any, idx: number) => (
                    <div key={idx} className="flex items-center justify-between p-5 hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors">
                      <div className="flex items-center gap-4">
                        <div className="rounded-full p-2.5 bg-slate-100 dark:bg-slate-800">
                          <Landmark className="h-4 w-4 text-slate-600 dark:text-slate-400" />
                        </div>
                        <div>
                          <p className="font-semibold text-slate-900 dark:text-white">
                            ₹{Number(log.amount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                          </p>
                          <p className="text-xs text-slate-500 mt-0.5">
                            {new Date(log.createdAt).toLocaleString('en-IN', {
                              day: 'numeric', month: 'short', year: 'numeric',
                              hour: '2-digit', minute: '2-digit'
                            })}
                          </p>
                        </div>
                      </div>
                      <div className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-bold uppercase ${statusColor(log.status)}`}>
                        {statusIcon(log.status)}
                        {log.status}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Right column */}
        <div className="space-y-6">

          {/* Bank details */}
          <Card className="border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-xl rounded-[2rem]">
            <CardHeader className="border-b border-slate-100 dark:border-slate-800/50 pb-5">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className="rounded-xl bg-slate-100 dark:bg-slate-800 p-2.5">
                    <Landmark className="h-5 w-5 text-blue-600 dark:text-blue-400" />
                  </div>
                  <div>
                    <CardTitle className="text-slate-900 dark:text-white text-lg">Bank Details</CardTitle>
                    <CardDescription className="text-slate-500">Your payout destination</CardDescription>
                  </div>
                </div>
                {hasBank && !isEditingBank && (
                  <button
                    onClick={() => setIsEditingBank(true)}
                    className="p-2 rounded-xl text-slate-400 hover:text-blue-600 hover:bg-blue-50 dark:hover:bg-blue-500/10 transition-colors"
                  >
                    <Edit2 className="w-4 h-4" />
                  </button>
                )}
              </div>
            </CardHeader>
            <CardContent className="pt-6">
              {!isEditingBank && hasBank ? (
                <div className="space-y-3">
                  {[
                    { label: 'Account Holder', value: bankDetails.accountHolderName },
                    { label: 'Bank', value: bankDetails.bankName },
                    { label: 'Account No.', value: bankDetails.accountNumber ? `•••• ${bankDetails.accountNumber.slice(-4)}` : '—' },
                    { label: 'IFSC', value: bankDetails.ifscCode || '—' },
                    { label: 'UPI ID', value: bankDetails.upiId || '—' },
                  ].map(row => (
                    <div key={row.label} className="flex justify-between items-center text-sm py-2 border-b border-slate-100 dark:border-slate-800 last:border-0">
                      <span className="text-slate-500 dark:text-slate-400 font-medium">{row.label}</span>
                      <span className="text-slate-900 dark:text-white font-semibold">{row.value}</span>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="space-y-4">
                  {[
                    { key: 'accountHolderName', label: 'Account Holder Name', placeholder: 'e.g. John Doe' },
                    { key: 'bankName', label: 'Bank Name', placeholder: 'e.g. State Bank of India' },
                    { key: 'accountNumber', label: 'Account Number', placeholder: '123456789', type: 'number' },
                    { key: 'ifscCode', label: 'IFSC Code', placeholder: 'SBIN0000123' },
                    { key: 'upiId', label: 'UPI ID (optional)', placeholder: 'user@bank' },
                  ].map(field => (
                    <div key={field.key}>
                      <label className="block text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1.5">
                        {field.label}
                      </label>
                      <input
                        type={field.type || 'text'}
                        placeholder={field.placeholder}
                        value={(bankDetails as any)[field.key]}
                        onChange={e => setBankDetails(prev => ({ ...prev, [field.key]: e.target.value }))}
                        className="w-full bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-xl px-4 py-3 text-sm text-slate-900 dark:text-white placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
                      />
                    </div>
                  ))}

                  <div className="flex gap-3 pt-2">
                    <Button
                      onClick={handleSaveBankDetails}
                      disabled={saving}
                      className="flex-1 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-bold shadow-md shadow-blue-500/20"
                    >
                      {saving ? 'Saving...' : hasBank ? 'Update Details' : 'Save Details'}
                    </Button>
                    {hasBank && (
                      <Button
                        variant="outline"
                        onClick={() => setIsEditingBank(false)}
                        className="rounded-xl border-slate-200 dark:border-slate-800"
                      >
                        Cancel
                      </Button>
                    )}
                  </div>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Ledger transactions */}
          <Card className="border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-xl rounded-[2rem] overflow-hidden">
            <CardHeader className="border-b border-slate-100 dark:border-slate-800/50 pb-5">
              <div className="flex items-center gap-3">
                <div className="rounded-xl bg-slate-100 dark:bg-slate-800 p-2.5">
                  <FileText className="h-5 w-5 text-slate-600 dark:text-slate-300" />
                </div>
                <div>
                  <CardTitle className="text-slate-900 dark:text-white text-lg">Ledger</CardTitle>
                  <CardDescription className="text-slate-500">All credits & debits</CardDescription>
                </div>
              </div>
              <div className="relative mt-3">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
                <input
                  type="text"
                  placeholder="Search..."
                  value={search}
                  onChange={e => setSearch(e.target.value)}
                  className="w-full pl-9 pr-4 py-2 rounded-full text-sm bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
                />
              </div>
            </CardHeader>
            <CardContent className="p-0 max-h-96 overflow-y-auto">
              {filteredTx.length === 0 ? (
                <div className="p-10 text-center text-slate-500 text-sm">No transactions found.</div>
              ) : (
                <div className="divide-y divide-slate-100 dark:divide-slate-800">
                  {filteredTx.map((tx: any) => {
                    const isCredit = tx.transactionType === 'CREDIT';
                    return (
                      <div key={tx.id} className="flex items-center justify-between p-4 hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors">
                        <div className="flex items-center gap-3">
                          <div className={`rounded-full p-2 ${isCredit ? 'bg-emerald-50 dark:bg-emerald-500/10 text-emerald-600' : 'bg-rose-50 dark:bg-rose-500/10 text-rose-600'}`}>
                            {isCredit ? <ArrowDownRight className="h-4 w-4" /> : <ArrowUpRight className="h-4 w-4" />}
                          </div>
                          <div>
                            <p className="text-sm font-semibold text-slate-900 dark:text-white">
                              {tx.description || (isCredit ? 'Credit' : 'Debit')}
                            </p>
                            <p className="text-xs text-slate-500 mt-0.5">
                              {new Date(tx.createdAt).toLocaleDateString('en-IN')} · {tx.referenceType}
                            </p>
                          </div>
                        </div>
                        <p className={`font-bold text-sm ${isCredit ? 'text-emerald-600 dark:text-emerald-400' : 'text-slate-700 dark:text-slate-300'}`}>
                          {isCredit ? '+' : '-'}₹{Math.abs(Number(tx.amount)).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                        </p>
                      </div>
                    );
                  })}
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
