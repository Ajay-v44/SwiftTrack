"use client";

import React, { useState, useEffect } from 'react';
import { 
  CheckCircle2, Clock, MapPin, Navigation, 
  Package, Star, Truck, Zap, Wallet, ArrowRightLeft, CreditCard, TrendingUp, User
} from 'lucide-react';
import { getProviderOnboardingStatusApi, requestProviderOnboardingApi, getAccountSummaryApi } from '@swifttrack/api-client';
import { useAuthStore } from '@/store/useAuthStore';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import Link from 'next/link';

export default function ProviderDashboard() {
  const { user } = useAuthStore();
  const [loading, setLoading] = useState(true);
  const [onboardingStatus, setOnboardingStatus] = useState<string | null>(null);
  const [providerDetails, setProviderDetails] = useState<any>(null);
  const [financeSummary, setFinanceSummary] = useState<any>(null);

  // Onboarding form state
  const [formData, setFormData] = useState({
    providerName: '',
    providerWebsite: '',
    contactEmail: '',
    contactPhone: '',
    notes: '',
    docLinks: '{}'
  });
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchStatus();
  }, []);

  const fetchStatus = async () => {
    try {
      setLoading(true);
      const res = await getProviderOnboardingStatusApi();
      if (res && res.data) {
        setOnboardingStatus(res.data.status);
        setProviderDetails(res.data);
        if (res.data.status === 'APPROVED') {
          fetchFinanceSummary();
        }
      } else {
        setOnboardingStatus(null);
      }
    } catch (e: any) {
      if (e?.response?.status === 404 || !e?.response) {
        setOnboardingStatus(null);
      } else {
        setOnboardingStatus('ERROR'); // fallback
      }
    } finally {
      setLoading(false);
    }
  };

  const fetchFinanceSummary = async () => {
    try {
      const res = await getAccountSummaryApi();
      if (res && res.data) {
        setFinanceSummary(res.data);
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await requestProviderOnboardingApi(formData);
      setOnboardingStatus('PENDING');
    } catch (err) {
      console.error(err);
      alert('Failed to submit onboarding request.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center p-12 text-slate-400">
        Loading...
      </div>
    );
  }

  if (onboardingStatus === 'PENDING') {
    return (
      <div className="p-12 text-center flex flex-col items-center justify-center min-h-[60vh]">
        <Clock className="w-16 h-16 text-blue-500 mb-6 animate-pulse" />
        <h2 className="text-3xl font-bold text-slate-800 dark:text-white mb-4">Onboarding in Review</h2>
        <p className="text-slate-500 dark:text-slate-400 max-w-md mx-auto">
          Your provider account request is currently under review by our administrators. We will notify you once you are approved.
        </p>
      </div>
    );
  }

  if (onboardingStatus === null) {
    return (
      <div className="p-12 max-w-3xl mx-auto">
        <h2 className="text-4xl font-bold text-slate-800 dark:text-white mb-8">Provider Onboarding</h2>
        <form onSubmit={handleSubmit} className="space-y-6 bg-white dark:bg-slate-900 p-8 rounded-[2rem] border border-slate-200 dark:border-slate-800 shadow-xl">
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-bold text-slate-700 dark:text-slate-300 mb-2">Provider/Company Name</label>
              <input required value={formData.providerName} onChange={e => setFormData({...formData, providerName: e.target.value})} className="w-full bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-xl py-3 px-4 text-slate-900 dark:text-white placeholder:text-slate-400 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-all" placeholder="e.g. Acme Logistics" />
            </div>
            <div>
              <label className="block text-sm font-bold text-slate-700 dark:text-slate-300 mb-2">Website (Optional)</label>
              <input value={formData.providerWebsite} onChange={e => setFormData({...formData, providerWebsite: e.target.value})} className="w-full bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-xl py-3 px-4 text-slate-900 dark:text-white placeholder:text-slate-400 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-all" placeholder="e.g. https://acmelogistics.com" />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-bold text-slate-700 dark:text-slate-300 mb-2">Contact Email</label>
                <input type="email" required value={formData.contactEmail} onChange={e => setFormData({...formData, contactEmail: e.target.value})} className="w-full bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-xl py-3 px-4 text-slate-900 dark:text-white placeholder:text-slate-400 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-all" placeholder="contact@acmelogistics.com" />
              </div>
              <div>
                <label className="block text-sm font-bold text-slate-700 dark:text-slate-300 mb-2">Contact Phone</label>
                <input required value={formData.contactPhone} onChange={e => setFormData({...formData, contactPhone: e.target.value})} className="w-full bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-xl py-3 px-4 text-slate-900 dark:text-white placeholder:text-slate-400 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-all" placeholder="+1 234 567 8900" />
              </div>
            </div>
            <div>
              <label className="block text-sm font-bold text-slate-700 dark:text-slate-300 mb-2">Notes</label>
              <textarea value={formData.notes} onChange={e => setFormData({...formData, notes: e.target.value})} rows={4} className="w-full bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-xl py-3 px-4 text-slate-900 dark:text-white placeholder:text-slate-400 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-all" placeholder="Tell us about your fleet and capabilities..." />
            </div>
          </div>
          <button disabled={submitting} type="submit" className="w-full py-4 bg-blue-600 hover:bg-blue-700 text-white font-black rounded-xl shadow-lg shadow-blue-500/30 hover:shadow-blue-500/50 hover:scale-[1.01] transition-all">
            {submitting ? 'Submitting...' : 'Request Onboarding'}
          </button>
        </form>
      </div>
    );
  }

  // APPROVED STATUS - Show Dashboard
  const balance = financeSummary?.balance || 0;
  return (
    <div className="p-8 md:p-12 space-y-8 min-h-screen bg-slate-50 dark:bg-slate-950">
      <h1 className="text-3xl font-bold text-slate-900 dark:text-white tracking-tight">Provider Dashboard</h1>
      
      <section className="grid gap-6 xl:grid-cols-[minmax(0,1.45fr)_minmax(320px,0.9fr)]">
        {/* Earnings & Wallet Shortcut */}
        <Card className="overflow-hidden border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-xl relative rounded-[2rem]">
           <div className="absolute left-0 bottom-0 w-96 h-96 bg-blue-500/5 rounded-full blur-[100px] pointer-events-none"></div>
           <CardContent className="flex flex-col gap-8 px-6 py-8 sm:px-8 relative z-10">
             <div className="flex flex-col gap-5 sm:flex-row sm:items-start sm:justify-between">
               <div className="space-y-3">
                 <Badge variant="outline" className="rounded-full border-blue-200 dark:border-blue-500/30 bg-blue-50 dark:bg-blue-500/10 px-3 py-1 text-blue-700 dark:text-blue-400">
                   Earnings Hub
                 </Badge>
                 <div className="space-y-2">
                   <p className="text-xs font-medium uppercase tracking-[0.24em] text-slate-500 dark:text-slate-400">Available Balance</p>
                   <div className="text-4xl font-semibold tracking-tight text-slate-900 dark:text-white sm:text-5xl">
                     ${balance.toLocaleString('en-US', {minimumFractionDigits: 2})}
                   </div>
                   <p className="max-w-lg text-sm leading-6 text-slate-600 dark:text-slate-300">
                     Monitor your earnings, withdraw funds, and view your payout history in your dedicated wallet.
                   </p>
                 </div>
               </div>
             </div>

             <div className="flex flex-wrap gap-3">
               <Link href="/provider/wallet">
                 <Button className="rounded-full bg-blue-600 text-white hover:bg-blue-700 font-bold border-none transition-transform hover:scale-105 active:scale-95 shadow-md shadow-blue-500/20">
                   <Wallet className="h-4 w-4 mr-2" />
                   Go to Wallet
                 </Button>
               </Link>
             </div>
           </CardContent>
        </Card>

        {/* Show current provider details */}
        <Card className="border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-xl rounded-[2rem]">
          <CardHeader className="border-b border-slate-100 dark:border-slate-800/50 pb-5">
            <div className="flex items-center gap-3">
              <div className="rounded-2xl bg-slate-100 dark:bg-slate-800 p-3 text-blue-600 dark:text-blue-400">
                <User className="h-5 w-5" />
              </div>
              <div>
                <CardTitle className="text-slate-900 dark:text-white text-lg">Your Details</CardTitle>
                <CardDescription className="text-slate-500 dark:text-slate-400">Company & Contact Info</CardDescription>
              </div>
            </div>
          </CardHeader>
          <CardContent className="space-y-5 pt-6">
            {providerDetails && (
              <div className="rounded-2xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-950/50 p-5 space-y-4">
                <div className="flex justify-between items-center">
                  <div className="font-bold text-slate-900 dark:text-white text-lg">{providerDetails.providerName}</div>
                  <Badge className="bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-none">Approved</Badge>
                </div>
                
                <div className="space-y-2 text-sm">
                  <div className="flex items-center gap-2 text-slate-600 dark:text-slate-300">
                    <span className="font-medium">Email:</span> {providerDetails.contactEmail}
                  </div>
                  <div className="flex items-center gap-2 text-slate-600 dark:text-slate-300">
                    <span className="font-medium">Phone:</span> {providerDetails.contactPhone}
                  </div>
                  {providerDetails.providerWebsite && (
                    <div className="flex items-center gap-2 text-slate-600 dark:text-slate-300">
                      <span className="font-medium">Website:</span> <a href={providerDetails.providerWebsite} target="_blank" rel="noreferrer" className="text-blue-500 hover:underline">{providerDetails.providerWebsite}</a>
                    </div>
                  )}
                </div>
              </div>
            )}
            {!providerDetails && (
              <div className="text-sm text-slate-500">Details not available.</div>
            )}
            <Button variant="outline" className="w-full rounded-full border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 text-slate-900 dark:text-white hover:bg-slate-50 dark:hover:bg-slate-800">
               Edit Profile
            </Button>
          </CardContent>
        </Card>
      </section>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4 mt-8">
        <Card className="border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-lg rounded-3xl transition-transform hover:-translate-y-1 duration-200">
          <CardContent className="p-6">
            <div className="rounded-2xl bg-blue-50 dark:bg-blue-500/10 w-12 h-12 flex items-center justify-center text-blue-600 dark:text-blue-400 mb-4">
              <TrendingUp className="w-6 h-6" />
            </div>
            <p className="text-xs font-bold uppercase tracking-widest text-slate-500 dark:text-slate-400">Total Earnings</p>
            {/* For a provider, their earnings are their balance or combined deposits. Here we just show the weekly spend equivalent as an earnings metric if that maps to it, or balance. */}
            <p className="text-3xl font-extrabold text-slate-900 dark:text-white mt-1">${(financeSummary?.weeklySpend || 0).toLocaleString('en-US', {minimumFractionDigits: 2})}</p>
          </CardContent>
        </Card>

        <Card className="border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-lg rounded-3xl transition-transform hover:-translate-y-1 duration-200">
          <CardContent className="p-6">
            <div className="rounded-2xl bg-emerald-50 dark:bg-emerald-500/10 w-12 h-12 flex items-center justify-center text-emerald-600 dark:text-emerald-400 mb-4">
              <ArrowRightLeft className="w-6 h-6" />
            </div>
            <p className="text-xs font-bold uppercase tracking-widest text-slate-500 dark:text-slate-400">Payouts Received</p>
            <p className="text-3xl font-extrabold text-slate-900 dark:text-white mt-1">${(financeSummary?.costSavings || 0).toLocaleString('en-US', {minimumFractionDigits: 2})}</p>
          </CardContent>
        </Card>

        <Card className="border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-lg rounded-3xl transition-transform hover:-translate-y-1 duration-200">
          <CardContent className="p-6">
            <div className="rounded-2xl bg-rose-50 dark:bg-rose-500/10 w-12 h-12 flex items-center justify-center text-rose-600 dark:text-rose-400 mb-4">
              <Clock className="w-6 h-6" />
            </div>
            <p className="text-xs font-bold uppercase tracking-widest text-slate-500 dark:text-slate-400">Pending Payouts</p>
            <p className="text-3xl font-extrabold text-slate-900 dark:text-white mt-1">${(financeSummary?.unpaidDues || 0).toLocaleString('en-US', {minimumFractionDigits: 2})}</p>
          </CardContent>
        </Card>

        <Card className="border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-lg rounded-3xl transition-transform hover:-translate-y-1 duration-200">
          <CardContent className="p-6">
            <div className="rounded-2xl bg-indigo-50 dark:bg-indigo-500/10 w-12 h-12 flex items-center justify-center text-indigo-600 dark:text-indigo-400 mb-4">
              <Package className="w-6 h-6" />
            </div>
            <p className="text-xs font-bold uppercase tracking-widest text-slate-500 dark:text-slate-400">Completed Orders</p>
            <p className="text-3xl font-extrabold text-slate-900 dark:text-white mt-1">{financeSummary?.invoiceCount || 0}</p>
          </CardContent>
        </Card>
      </div>

    </div>
  );
}
