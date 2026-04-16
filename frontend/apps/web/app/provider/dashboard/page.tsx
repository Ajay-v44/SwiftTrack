"use client";

import React, { useState, useEffect } from 'react';
import { 
  CheckCircle2, Clock, MapPin, Navigation, 
  Package, Star, Truck, Zap, Wallet, ArrowRightLeft, CreditCard, TrendingUp
} from 'lucide-react';
import { getProviderOnboardingStatusApi, requestProviderOnboardingApi, getProviderByStatusApi } from '@swifttrack/api-client';
import { useAuthStore } from '@/store/useAuthStore';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";

export default function ProviderDashboard() {
  const { user } = useAuthStore();
  const [loading, setLoading] = useState(true);
  const [onboardingStatus, setOnboardingStatus] = useState<string | null>(null);
  const [acceptedProviders, setAcceptedProviders] = useState<any[]>([]);

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
        if (res.data.status === 'APPROVED') {
          fetchAcceptedProviders();
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

  const fetchAcceptedProviders = async () => {
    try {
      const res = await getProviderByStatusApi(true);
      if (res && res.data) {
        setAcceptedProviders(res.data);
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
      <div className="flex min-h-screen items-center justify-center p-12 text-[#d3c2c1]">
        Loading...
      </div>
    );
  }

  if (onboardingStatus === 'PENDING') {
    return (
      <div className="p-12 text-center text-[#d3c2c1] flex flex-col items-center justify-center min-h-[60vh]">
        <Clock className="w-16 h-16 text-amber-500 mb-6 animate-pulse" />
        <h2 className="text-3xl font-bold text-white mb-4">Onboarding in Review</h2>
        <p className="text-[#d3c2c1]/80 max-w-md mx-auto">
          Your provider account request is currently under review by our administrators. We will notify you once you are approved.
        </p>
      </div>
    );
  }

  if (onboardingStatus === null) {
    return (
      <div className="p-12 text-[#d3c2c1] max-w-3xl mx-auto">
        <h2 className="text-4xl font-bold text-white mb-8">Provider Onboarding</h2>
        <form onSubmit={handleSubmit} className="space-y-6 bg-[#1B1212] p-8 rounded-[2rem] border border-[#ffb4ab]/10 shadow-2xl">
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-bold text-[#d3c2c1] mb-2">Provider/Company Name</label>
              <input required value={formData.providerName} onChange={e => setFormData({...formData, providerName: e.target.value})} className="w-full bg-[#2a1b1b] border border-[#ffb4ab]/20 rounded-xl py-3 px-4 text-white placeholder:text-[#d3c2c1]/50 focus:outline-none focus:border-[#ffb4ab]" placeholder="e.g. Acme Logistics" />
            </div>
            <div>
              <label className="block text-sm font-bold text-[#d3c2c1] mb-2">Website (Optional)</label>
              <input value={formData.providerWebsite} onChange={e => setFormData({...formData, providerWebsite: e.target.value})} className="w-full bg-[#2a1b1b] border border-[#ffb4ab]/20 rounded-xl py-3 px-4 text-white placeholder:text-[#d3c2c1]/50 focus:outline-none focus:border-[#ffb4ab]" placeholder="e.g. https://acmelogistics.com" />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-bold text-[#d3c2c1] mb-2">Contact Email</label>
                <input type="email" required value={formData.contactEmail} onChange={e => setFormData({...formData, contactEmail: e.target.value})} className="w-full bg-[#2a1b1b] border border-[#ffb4ab]/20 rounded-xl py-3 px-4 text-white placeholder:text-[#d3c2c1]/50 focus:outline-none focus:border-[#ffb4ab]" placeholder="contact@acmelogistics.com" />
              </div>
              <div>
                <label className="block text-sm font-bold text-[#d3c2c1] mb-2">Contact Phone</label>
                <input required value={formData.contactPhone} onChange={e => setFormData({...formData, contactPhone: e.target.value})} className="w-full bg-[#2a1b1b] border border-[#ffb4ab]/20 rounded-xl py-3 px-4 text-white placeholder:text-[#d3c2c1]/50 focus:outline-none focus:border-[#ffb4ab]" placeholder="+1 234 567 8900" />
              </div>
            </div>
            <div>
              <label className="block text-sm font-bold text-[#d3c2c1] mb-2">Notes</label>
              <textarea value={formData.notes} onChange={e => setFormData({...formData, notes: e.target.value})} rows={4} className="w-full bg-[#2a1b1b] border border-[#ffb4ab]/20 rounded-xl py-3 px-4 text-white placeholder:text-[#d3c2c1]/50 focus:outline-none focus:border-[#ffb4ab]" placeholder="Tell us about your fleet and capabilities..." />
            </div>
          </div>
          <button disabled={submitting} type="submit" className="w-full py-4 bg-[#ffb4ab] text-[#410002] font-black rounded-xl shadow-[0_0_30px_rgba(255,180,171,0.3)] hover:scale-[1.02] transition-transform">
            {submitting ? 'Submitting...' : 'Request Onboarding'}
          </button>
        </form>
      </div>
    );
  }

  // APPROVED STATUS - Show Wallet/Dashboard
  const balance = 12500.00; // Mock balance
  return (
    <div className="p-12 space-y-8 text-[#d3c2c1]">
      <h1 className="text-3xl font-bold text-white tracking-tight">Provider Financials & Providers</h1>
      
      <section className="grid gap-6 xl:grid-cols-[minmax(0,1.45fr)_minmax(320px,0.9fr)]">
        <Card className="overflow-hidden border border-[#ffb4ab]/10 bg-gradient-to-br from-[#2a1b1b] to-[#1B1212] shadow-2xl relative rounded-[2rem]">
           <div className="absolute left-0 bottom-0 w-96 h-96 bg-[#ffb4ab]/5 rounded-full blur-[100px] pointer-events-none"></div>
           <CardContent className="flex flex-col gap-8 px-6 py-8 sm:px-8 relative z-10">
             <div className="flex flex-col gap-5 sm:flex-row sm:items-start sm:justify-between">
               <div className="space-y-3">
                 <Badge variant="outline" className="rounded-full border-[#ffb4ab]/20 bg-[#2a1b1b] px-3 py-1 text-[#ffb4ab]">
                   Finance Hub
                 </Badge>
                 <div className="space-y-2">
                   <p className="text-xs font-medium uppercase tracking-[0.24em] text-[#d3c2c1]/70">Available Balance</p>
                   <div className="text-4xl font-semibold tracking-tight text-white sm:text-5xl">
                     ${balance.toLocaleString('en-US', {minimumFractionDigits: 2})}
                   </div>
                   <p className="max-w-lg text-sm leading-6 text-[#d3c2c1]/80">
                     Monitor your earnings, withdraw funds, and view your active provider status details.
                   </p>
                 </div>
               </div>
             </div>

             <div className="flex flex-wrap gap-3">
               <Button className="rounded-full bg-[#ffb4ab] text-[#410002] hover:bg-[#ffb4ab]/80 font-bold border-none transition-transform hover:scale-105 active:scale-95">
                 <Wallet className="h-4 w-4 mr-2" />
                 Withdraw Earnings
               </Button>
               <Button variant="outline" className="rounded-full border-[#ffb4ab]/20 bg-[#2a1b1b] text-white hover:bg-[#ffb4ab]/10">
                 View Transactions
               </Button>
             </div>
           </CardContent>
        </Card>

        {/* Show active providers as requested */}
        <Card className="border border-[#ffb4ab]/10 bg-[#1B1212] shadow-2xl rounded-[2rem]">
          <CardHeader className="border-b border-[#ffb4ab]/10 pb-5">
            <div className="flex items-center gap-3">
              <div className="rounded-2xl bg-[#2a1b1b] p-3 text-[#ffb4ab]">
                <Truck className="h-5 w-5" />
              </div>
              <div>
                <CardTitle className="text-white text-lg">Active Networks</CardTitle>
                <CardDescription className="text-[#d3c2c1]/70">Your provider details & active networks</CardDescription>
              </div>
            </div>
          </CardHeader>
          <CardContent className="space-y-5 pt-6">
            {acceptedProviders.map(p => (
              <div key={p.id} className="rounded-2xl border border-[#ffb4ab]/10 bg-[#2a1b1b]/50 p-4">
                <div className="flex justify-between items-center">
                  <div className="font-medium text-white">{p.providerName}</div>
                  <Badge className="bg-emerald-500/20 text-emerald-400 border-none">Active</Badge>
                </div>
                {p.servicableAreas?.length > 0 && (
                   <p className="text-xs text-[#d3c2c1]/60 mt-2">Areas: {p.servicableAreas.join(', ')}</p>
                )}
              </div>
            ))}
            {acceptedProviders.length === 0 && (
              <div className="text-xs text-[#d3c2c1]/60">No active networks.</div>
            )}
            <Button variant="outline" className="w-full rounded-full border-[#ffb4ab]/20 bg-[#2a1b1b] text-white hover:bg-[#ffb4ab]/10">
               Manage Network
            </Button>
          </CardContent>
        </Card>
      </section>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4 mt-8">
        <Card className="border border-[#ffb4ab]/10 bg-[#1B1212] shadow-xl rounded-3xl">
          <CardContent className="p-6">
            <div className="rounded-2xl bg-[#ffb4ab]/10 w-12 h-12 flex items-center justify-center text-[#ffb4ab] mb-4">
              <TrendingUp className="w-6 h-6" />
            </div>
            <p className="text-xs font-bold uppercase tracking-widest text-[#d3c2c1]/70">Total Revenue</p>
            <p className="text-3xl font-extrabold text-white mt-1">$45,230</p>
          </CardContent>
        </Card>
      </div>

    </div>
  );
}
