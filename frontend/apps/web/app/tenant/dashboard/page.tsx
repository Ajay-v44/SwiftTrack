"use client";

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { useAuthStore } from '@/store/useAuthStore';
import apiClient from '@/lib/api-client';
import { 
  Wallet, Package2, Rocket, TrendingUp,
  FileText, PlusCircle, CreditCard, Clock, MapPin, CheckCircle
} from 'lucide-react';
import { toast } from 'sonner';

export default function TenantDashboard() {
  const { user } = useAuthStore();
  const [walletBalance, setWalletBalance] = useState<number>(0);
  
  useEffect(() => {
    async function fetchDashboardData() {
      try {
        if (!user?.id) return;
        // Fetch Tenant Account balance
        const response = await apiClient.get('/api/accounts/v1/getMyAccount', {
          params: { userId: user.id }
        });
        setWalletBalance(response.data?.balance || 0);
      } catch (error) {
        console.error("Dashboard data error:", error);
      }
    }
    fetchDashboardData();
  }, [user]);

  return (
    <div className="p-12 space-y-8 text-[#dae2fd]">
      
      {/* Top Overview Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        
        {/* Wallet Balance */}
        <div className="bg-[#131b2e] border border-white/5 p-6 rounded-[2rem] shadow-xl relative overflow-hidden group hover:-translate-y-1 transition-transform">
          <div className="flex justify-between items-start mb-4">
            <h3 className="text-[10px] font-black uppercase tracking-widest text-[#bac3ff]">Current Wallet Balance</h3>
            <div className="w-8 h-8 rounded-lg bg-[#3e5bf2]/20 flex items-center justify-center text-[#3e5bf2]">
              <Wallet className="w-4 h-4" />
            </div>
          </div>
          <p className="font-['Manrope'] text-4xl font-extrabold text-white mb-2">
            ${walletBalance.toLocaleString('en-US', { minimumFractionDigits: 2 })}
          </p>
          <p className="text-xs font-bold text-[#00dce5] flex items-center gap-1">
            <TrendingUp className="w-3 h-3" /> +12% from last week
          </p>
        </div>

        {/* Total Orders */}
        <div className="bg-[#131b2e] border border-white/5 p-6 rounded-[2rem] shadow-xl relative overflow-hidden group hover:-translate-y-1 transition-transform">
          <div className="flex justify-between items-start mb-4">
            <h3 className="text-[10px] font-black uppercase tracking-widest text-[#c5c5d8]">Total Orders</h3>
            <div className="w-8 h-8 rounded-lg bg-[#00dce5]/20 flex items-center justify-center text-[#00dce5]">
              <Package2 className="w-4 h-4" />
            </div>
          </div>
          <p className="font-['Manrope'] text-4xl font-extrabold text-white mb-2">1,280</p>
          <p className="text-xs text-[#8e8fa1]">Processed this month</p>
        </div>

        {/* Active Deliveries */}
        <div className="bg-[#131b2e] border border-white/5 p-6 rounded-[2rem] shadow-xl relative overflow-hidden group hover:-translate-y-1 transition-transform">
          <div className="flex justify-between items-start mb-4">
            <h3 className="text-[10px] font-black uppercase tracking-widest text-[#c5c5d8]">Active Deliveries</h3>
            <div className="w-8 h-8 rounded-lg bg-emerald-500/20 flex items-center justify-center text-emerald-400">
              <Rocket className="w-4 h-4" />
            </div>
          </div>
          <p className="font-['Manrope'] text-4xl font-extrabold text-white mb-2">45</p>
          <p className="text-xs text-emerald-400">Live tracking active</p>
        </div>

        {/* Recent Expenses */}
        <div className="bg-[#131b2e] border border-white/5 p-6 rounded-[2rem] shadow-xl relative overflow-hidden group hover:-translate-y-1 transition-transform">
          <div className="flex justify-between items-start mb-4">
            <h3 className="text-[10px] font-black uppercase tracking-widest text-[#c5c5d8]">Recent Expenses</h3>
            <div className="w-8 h-8 rounded-lg bg-[#ffb4ab]/20 flex items-center justify-center text-[#ffb4ab]">
              <TrendingUp className="w-4 h-4 rotate-45" />
            </div>
          </div>
          <p className="font-['Manrope'] text-4xl font-extrabold text-white mb-2">$890.50</p>
          <p className="text-xs text-[#ffb4ab]">Past 24 hours</p>
        </div>
      </div>

      {/* Action Banner */}
      <div className="bg-[#171f33] border border-white/5 rounded-[2rem] p-10 flex flex-col md:flex-row items-center justify-between shadow-2xl relative overflow-hidden gap-10">
        <div className="z-10 absolute -top-40 -left-20 w-80 h-80 bg-[#3e5bf2]/10 rounded-full blur-[80px]"></div>
        
        <div className="max-w-xl z-20">
          <h2 className="font-['Manrope'] text-3xl font-extrabold text-white mb-4">Fleet Command</h2>
          <p className="text-[#c5c5d8] text-lg leading-relaxed">
            Execute essential logistics operations or fund your account instantly through our priority gateway.
          </p>
        </div>

        <div className="flex flex-wrap gap-4 z-20">
          <Link href="/tenant/orders/create">
            <div className="px-6 py-4 bg-[#00dce5] text-[#002021] font-bold rounded-full hover:bg-[#00dce5]/90 transition-colors flex items-center gap-2 text-sm shadow-[0_0_20px_rgba(0,220,229,0.3)]">
              <FileText className="w-4 h-4" />
              Request Quote
            </div>
          </Link>
          <Link href="/tenant/orders/create">
            <div className="px-6 py-4 bg-[#2d3449] border border-[#444655] text-white font-bold rounded-full hover:bg-[#3e495d] transition-colors flex items-center gap-2 text-sm">
              <PlusCircle className="w-4 h-4" />
              Create Order
            </div>
          </Link>
          <Link href="/tenant/finance">
            <div className="px-6 py-4 bg-[#2d3449] border border-[#444655] text-[#bac3ff] font-bold rounded-full hover:bg-[#3e495d] transition-colors flex items-center gap-2 text-sm">
              <CreditCard className="w-4 h-4" />
              Top-up Wallet
            </div>
          </Link>
        </div>
      </div>

      {/* Grid Data */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Graph Placeholder */}
        <div className="lg:col-span-2 bg-[#131b2e] border border-white/5 rounded-[2rem] p-8 shadow-xl">
          <div className="flex items-center justify-between mb-8">
            <div>
              <h3 className="font-['Manrope'] text-xl font-bold text-white">Delivery Volume</h3>
              <p className="text-xs text-[#c5c5d8] mt-1">Operations overview for the last 30 days</p>
            </div>
            <div className="bg-[#2d3449] rounded-full p-1 flex gap-1">
              <button className="px-4 py-1.5 rounded-full text-xs font-bold text-white bg-[#3e5bf2]">Monthly</button>
              <button className="px-4 py-1.5 rounded-full text-xs font-bold text-[#8e8fa1] hover:text-white">Weekly</button>
            </div>
          </div>
          
          <div className="w-full h-64 flex items-end justify-between px-4 pb-2 border-b border-[#2d3449] relative">
            <div className="absolute w-full h-full left-0 bottom-0 pointer-events-none opacity-20 bg-[url('data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI0MCIgaGVpZ2h0PSI0MCI+ICAgIDxwYXRoIGQ9Ik0wIDEwaDQwdi0xSDB6bTAgMjBoNDB2LTFIMHoiIHN0cm9rZT0iIzk5OSIgZmlsbD0ibm9uZSIgb3BhY2l0eT0iLjIiLz48L3N2Zz4=')]"></div>
            {/* Mock Bars */}
            {[40, 60, 45, 80, 70, 95, 60, 110].map((h, i) => (
              <div 
                key={i} 
                className={`w-12 rounded-t-xl transition-all duration-1000 ${i % 2 === 0 ? 'bg-gradient-to-t from-[#3e5bf2] to-[#bac3ff]' : 'bg-gradient-to-t from-[#00787d] to-[#00dce5]'}`}
                style={{ height: `${h}%` }}
              ></div>
            ))}
          </div>
        </div>

        {/* Live Status Tracker */}
        <div className="bg-[#131b2e] border border-white/5 rounded-[2rem] p-8 shadow-xl">
          <h3 className="font-['Manrope'] text-xl font-bold text-white mb-8">Live Status</h3>
          
          <div className="space-y-6">
            <div className="bg-[#171f33] p-5 rounded-2xl flex items-center justify-between border border-[#3e5bf2]/20">
              <div className="flex items-center gap-4">
                <div className="w-10 h-10 rounded-full bg-[#3e5bf2]/20 text-[#3e5bf2] flex items-center justify-center">
                  <Rocket className="w-5 h-5" />
                </div>
                <div>
                  <p className="text-sm font-bold text-white">#TRK-88021</p>
                  <p className="text-[10px] text-[#c5c5d8]">In Transit - Austin, TX</p>
                </div>
              </div>
              <div className="w-2 h-2 rounded-full bg-[#00dce5] animate-pulse"></div>
            </div>

            <div className="bg-[#171f33] p-5 rounded-2xl flex items-center justify-between border border-[#2d3449]">
              <div className="flex items-center gap-4">
                <div className="w-10 h-10 rounded-full bg-[#2d3449] text-[#c5c5d8] flex items-center justify-center">
                  <MapPin className="w-5 h-5" />
                </div>
                <div>
                  <p className="text-sm font-bold text-white">#TRK-90124</p>
                  <p className="text-[10px] text-[#c5c5d8]">Sorting - Hub Central</p>
                </div>
              </div>
              <div className="w-2 h-2 rounded-full bg-[#c5c5d8]"></div>
            </div>

            <div className="bg-[#171f33] p-5 rounded-2xl flex items-center justify-between border border-[#2d3449]">
              <div className="flex items-center gap-4">
                <div className="w-10 h-10 rounded-full bg-[#2d3449] text-emerald-400 flex items-center justify-center">
                  <CheckCircle className="w-5 h-5" />
                </div>
                <div>
                  <p className="text-sm font-bold text-white">#TRK-77211</p>
                  <p className="text-[10px] text-[#c5c5d8]">Delivered - New York, NY</p>
                </div>
              </div>
              <div className="w-4 h-4 rounded-full bg-emerald-500/20 text-emerald-400 flex items-center justify-center"><CheckCircle className="w-3 h-3"/></div>
            </div>

            <button className="w-full py-4 mt-2 bg-[#171f33] hover:bg-[#2d3449] text-white font-bold rounded-xl text-sm transition-colors border border-white/5">
              View Detailed Map
            </button>
          </div>
        </div>
      </div>

    </div>
  );
}
