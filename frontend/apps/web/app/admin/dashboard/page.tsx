"use client";

import React, { useEffect, useState } from 'react';
import { 
  Building2, Users, Activity,
  ServerCrash, Database, Network, ShieldCheck
} from 'lucide-react';

export default function AdminDashboard() {
  const [metrics, setMetrics] = useState({
    totalTenants: 124,
    activeProviders: 48,
    systemUptime: 99.99,
    apiRequests: 2154432,
  });

  return (
    <div className="p-12 space-y-8 text-[#dae2fd]">
      
      {/* Header */}
       <div className="flex flex-col md:flex-row gap-6 mb-8 justify-between items-start">
        <div>
           <h1 className="font-['Manrope'] text-3xl font-extrabold text-white mb-2">Platform Telemetry</h1>
           <p className="text-indigo-300 text-sm">Real-time surveillance of SwiftTrack infrastructure and global tenant operations.</p>
        </div>
        <div className="flex gap-4">
          <button className="px-6 py-2.5 bg-indigo-500/20 text-indigo-400 font-bold rounded-xl text-xs flex items-center gap-2 border border-indigo-500/30 shadow-[0_0_20px_rgba(99,102,241,0.2)]">
            <Activity className="w-4 h-4" /> Run Health Check
          </button>
        </div>
      </div>

      {/* Top Overview Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        
        <div className="bg-[#0b0f19] border border-white/5 p-6 rounded-3xl shadow-2xl relative overflow-hidden group">
          <div className="absolute right-0 top-0 w-24 h-24 bg-gradient-to-br from-indigo-500/20 to-transparent blur-2xl"></div>
          <div className="flex justify-between items-start mb-4">
            <h3 className="text-[10px] font-black uppercase tracking-widest text-[#8e8fa1]">Total Tenants</h3>
            <div className="w-8 h-8 rounded-xl bg-indigo-500/10 flex items-center justify-center text-indigo-400">
              <Building2 className="w-4 h-4" />
            </div>
          </div>
          <p className="font-['Manrope'] text-4xl font-extrabold text-white mb-2">{metrics.totalTenants}</p>
          <p className="text-[10px] font-bold text-emerald-400 uppercase tracking-wider">+12 this month</p>
        </div>

        <div className="bg-[#0b0f19] border border-white/5 p-6 rounded-3xl shadow-2xl relative overflow-hidden group">
          <div className="absolute right-0 top-0 w-24 h-24 bg-gradient-to-br from-purple-500/20 to-transparent blur-2xl"></div>
          <div className="flex justify-between items-start mb-4">
            <h3 className="text-[10px] font-black uppercase tracking-widest text-[#8e8fa1]">Active Providers</h3>
            <div className="w-8 h-8 rounded-xl bg-purple-500/10 flex items-center justify-center text-purple-400">
              <Network className="w-4 h-4" />
            </div>
          </div>
          <p className="font-['Manrope'] text-4xl font-extrabold text-white mb-2">{metrics.activeProviders}</p>
          <p className="text-[10px] font-bold text-purple-400 uppercase tracking-wider">Global endpoints</p>
        </div>

        <div className="bg-[#0b0f19] border border-emerald-500/20 p-6 rounded-3xl shadow-[0_0_30px_rgba(16,185,129,0.05)] relative overflow-hidden group">
          <div className="absolute right-0 top-0 w-24 h-24 bg-gradient-to-br from-emerald-500/20 to-transparent blur-2xl"></div>
          <div className="flex justify-between items-start mb-4">
            <h3 className="text-[10px] font-black uppercase tracking-widest text-emerald-400">System Uptime</h3>
            <div className="w-8 h-8 rounded-xl bg-emerald-500/10 flex items-center justify-center text-emerald-400">
              <ShieldCheck className="w-4 h-4" />
            </div>
          </div>
          <p className="font-['Manrope'] text-4xl font-extrabold text-white mb-2">{metrics.systemUptime}%</p>
          <p className="text-[10px] font-bold text-emerald-400 uppercase tracking-wider">Operational</p>
        </div>

        <div className="bg-[#0b0f19] border border-white/5 p-6 rounded-3xl shadow-2xl relative overflow-hidden group">
          <div className="absolute right-0 top-0 w-24 h-24 bg-gradient-to-br from-blue-500/20 to-transparent blur-2xl"></div>
          <div className="flex justify-between items-start mb-4">
            <h3 className="text-[10px] font-black uppercase tracking-widest text-[#8e8fa1]">API Requests (24h)</h3>
            <div className="w-8 h-8 rounded-xl bg-blue-500/10 flex items-center justify-center text-blue-400">
              <Database className="w-4 h-4" />
            </div>
          </div>
          <p className="font-['Manrope'] text-4xl font-extrabold text-white mb-2">{(metrics.apiRequests / 1000000).toFixed(1)}M</p>
          <p className="text-[10px] font-bold text-[#8e8fa1] uppercase tracking-wider">Avg Latency: 42ms</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mt-8">
        
        {/* Core Infrastructure Nodes */}
        <div className="bg-gradient-to-br from-[#0b0f19] to-[#131b2e] border border-indigo-500/10 rounded-3xl p-8 shadow-2xl">
           <div className="flex items-center justify-between mb-8">
            <h3 className="font-['Manrope'] text-xl font-bold text-white flex items-center gap-3">
              <ServerCrash className="w-5 h-5 text-indigo-400" /> Infrastructure Nodes
            </h3>
            <div className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></div>
          </div>
          
          <div className="space-y-4">
            {['Order Matching Engine', 'Billing & Ledger Service', 'Tenant Isolation Service', 'Mobile Driver WebSockets'].map((svc, i) => (
               <div key={svc} className="bg-[#050810] rounded-2xl p-4 border border-white/5 flex items-center justify-between">
                 <div className="flex flex-col">
                   <span className="text-sm font-bold text-white">{svc}</span>
                   <span className="text-[10px] text-[#8e8fa1] font-mono tracking-wider">prod-us-east-1a : 9 pods</span>
                 </div>
                 <div className="flex items-center gap-3">
                   <div className="text-right">
                     <p className="text-xs font-bold text-emerald-400">Healthy</p>
                     <p className="text-[10px] text-[#8e8fa1]">{20 + i * 15}ms</p>
                   </div>
                 </div>
               </div>
            ))}
          </div>
        </div>

        {/* Global Activity Map Placeholder */}
        <div className="bg-[#0b0f19] border border-white/5 rounded-3xl p-8 shadow-2xl relative overflow-hidden group">
          <div className="flex items-center justify-between mb-8 relative z-10">
            <h3 className="font-['Manrope'] text-xl font-bold text-white flex items-center gap-3">
              <Activity className="w-5 h-5 text-purple-400" /> Live Data Stream
            </h3>
            <button className="text-xs bg-white/5 px-4 py-1.5 rounded-full hover:bg-white/10 transition-colors">Expand</button>
          </div>

          <div className="absolute inset-0 top-20 opacity-30 bg-[url('https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=2072&auto=format&fit=crop')] bg-cover mix-blend-screen grayscale filter"></div>
          
          <div className="relative z-10 space-y-3 font-mono text-xs max-h-[250px] overflow-y-auto hidden-scrollbar mt-4">
             <div className="flex gap-4 items-center">
               <span className="text-emerald-400">200 OK</span>
               <span className="text-[#8e8fa1] w-24">11:42:01.002</span>
               <span className="text-indigo-300">/api/order/v1/tenant/quote</span>
               <span className="text-white ml-auto">12ms</span>
             </div>
             <div className="flex gap-4 items-center">
               <span className="text-emerald-400">200 OK</span>
               <span className="text-[#8e8fa1] w-24">11:42:01.105</span>
               <span className="text-indigo-300">/api/auth/v1/validate</span>
               <span className="text-white ml-auto">45ms</span>
             </div>
             <div className="flex gap-4 items-center bg-red-500/10 border border-red-500/20 p-2 rounded-lg -mx-2 px-2">
               <span className="text-red-400 font-bold">500 ERR</span>
               <span className="text-[#8e8fa1] w-20">11:42:02.394</span>
               <span className="text-red-300">/api/webhook/dhl/sync</span>
               <span className="text-white ml-auto">Timeout</span>
             </div>
             <div className="flex gap-4 items-center">
               <span className="text-emerald-400">201 CREATED</span>
               <span className="text-[#8e8fa1] w-20">11:42:04.901</span>
               <span className="text-indigo-300">/api/accounts/v1/topup</span>
               <span className="text-white ml-auto">201ms</span>
             </div>
             <div className="flex gap-4 items-center">
               <span className="text-emerald-400">200 OK</span>
               <span className="text-[#8e8fa1] w-24">11:42:05.112</span>
               <span className="text-indigo-300">/api/tenant/v1/list</span>
               <span className="text-white ml-auto">84ms</span>
             </div>
          </div>
        </div>

      </div>
    </div>
  );
}
