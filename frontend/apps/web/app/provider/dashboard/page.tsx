"use client";

import React, { useState } from 'react';
import { 
  Package, MapPin, Truck, CheckCircle2, Navigation,
  Activity, Star, Zap, DollarSign, Calendar
} from 'lucide-react';

export default function ProviderDashboard() {
  const [metrics] = useState({
    activeBids: 14,
    enRoute: 8,
    deliveredToday: 24,
    qualityScore: 98.5,
    grossEarnings: 3450.00
  });

  return (
    <div className="p-12 space-y-8 text-[#d3c2c1]">
      
      {/* Header Banner */}
       <div className="relative bg-gradient-to-br from-[#2a1b1b] to-[#1B1212] rounded-[3rem] p-12 border border-[#ffb4ab]/10 overflow-hidden shadow-2xl">
        <div className="absolute right-0 top-0 w-1/2 h-full bg-[url('https://images.unsplash.com/photo-1579450841249-1dbfc50f00ed?q=80&w=2070&auto=format&fit=crop')] mix-blend-overlay opacity-20 bg-cover bg-center"></div>
        <div className="absolute left-0 bottom-0 w-96 h-96 bg-[#ffb4ab]/10 rounded-full blur-[100px] pointer-events-none"></div>

        <div className="relative z-10 max-w-2xl">
          <p className="text-[#ffb4ab] font-bold tracking-widest text-[10px] uppercase mb-4 flex items-center gap-2">
            <Activity className="w-3 h-3" /> Real-time Logistics
          </p>
          <h1 className="font-['Manrope'] text-5xl font-extrabold text-white mb-6 leading-tight">
            Manage your fleet & capture dynamic loads.
          </h1>
          <div className="flex gap-4">
            <button className="px-8 py-4 bg-[#ffb4ab] text-[#410002] font-black rounded-full shadow-[0_0_30px_rgba(255,180,171,0.3)] hover:scale-105 transition-transform flex items-center gap-2">
              <Zap className="w-5 h-5 fill-[#410002]" /> Secure Open Bids
            </button>
            <button className="px-8 py-4 bg-[#2a1b1b] border border-[#ffb4ab]/20 text-[#ffb4ab] font-bold rounded-full hover:bg-[#ffb4ab]/10 transition-colors">
              Dispatch Assets
            </button>
          </div>
        </div>
      </div>

      {/* Primary Metrics */}
      <div className="grid grid-cols-1 md:grid-cols-5 gap-6">
        {[
          { label: 'Active Bids', value: metrics.activeBids, icon: Zap, color: 'text-amber-400', bg: 'bg-amber-400/10' },
          { label: 'Vehicles En Route', value: metrics.enRoute, icon: Truck, color: 'text-[#ffb4ab]', bg: 'bg-[#ffb4ab]/10' },
          { label: 'Delivered Today', value: metrics.deliveredToday, icon: CheckCircle2, color: 'text-emerald-400', bg: 'bg-emerald-400/10' },
          { label: 'Quality Score', value: `${metrics.qualityScore}%`, icon: Star, color: 'text-purple-400', bg: 'bg-purple-400/10' },
          { label: 'Today\'s Earnings', value: `$${metrics.grossEarnings.toLocaleString()}`, icon: DollarSign, color: 'text-white', bg: 'bg-white/10' }
        ].map((m, i) => (
          <div key={i} className="bg-[#1B1212] border border-[#ffb4ab]/5 p-6 rounded-3xl shadow-xl hover:-translate-y-1 transition-transform group">
            <div className={`w-10 h-10 rounded-xl ${m.bg} ${m.color} flex items-center justify-center mb-6`}>
              <m.icon className="w-5 h-5" />
            </div>
            <p className="font-['Manrope'] text-3xl font-extrabold text-white mb-1 group-hover:text-[#ffb4ab] transition-colors">{m.value}</p>
            <p className="text-[10px] font-bold uppercase tracking-widest text-[#d3c2c1]/70">{m.label}</p>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mt-8">
        
        {/* Open Marketplace Bids */}
        <div className="lg:col-span-2 bg-[#1B1212] border border-[#ffb4ab]/10 rounded-[2rem] p-8 shadow-2xl relative">
          <div className="flex justify-between items-center mb-8">
            <div>
              <h3 className="font-['Manrope'] text-xl font-bold text-white">Live Operations Board</h3>
              <p className="text-[#d3c2c1] text-xs mt-1">Accept and dispatch loads instantly</p>
            </div>
            <button className="text-xs font-bold text-[#ffb4ab] border border-[#ffb4ab] px-4 py-1.5 rounded-full hover:bg-[#ffb4ab]/10">View All Available</button>
          </div>

          <div className="space-y-4">
            {[1, 2, 3].map(bid => (
              <div key={bid} className="bg-[#2a1b1b]/50 border border-[#ffb4ab]/10 rounded-2xl p-5 hover:bg-[#2a1b1b] transition-colors flex items-center justify-between">
                <div className="flex gap-6 items-center w-full">
                  <div className="w-12 h-12 bg-[#ffb4ab]/10 rounded-xl flex items-center justify-center text-[#ffb4ab] shrink-0">
                    <Package className="w-6 h-6" />
                  </div>
                  <div className="flex-1 flex justify-between items-center">
                    <div>
                       <div className="flex items-center gap-2 mb-1">
                         <p className="font-bold text-white text-sm">Industrial Freight - 4,500 LBS</p>
                         <span className="bg-amber-500/20 text-amber-400 text-[10px] uppercase font-black px-2 py-0.5 rounded-md">Urgent SLA</span>
                       </div>
                       <div className="flex items-center gap-4 text-xs text-[#d3c2c1]/80 font-medium">
                         <span className="flex items-center gap-1"><MapPin className="w-3 h-3"/> Chicago, IL</span>
                         <span className="text-[#ffb4ab]">→</span>
                         <span className="flex items-center gap-1"><Navigation className="w-3 h-3"/> Detroit, MI</span>
                       </div>
                    </div>
                    <div className="text-right">
                       <p className="font-['Manrope'] text-2xl font-black text-white mb-1">$850.00</p>
                       <button className="px-6 py-2 bg-[#ffb4ab] text-[#410002] text-xs font-bold rounded-full shadow-[0_0_15px_rgba(255,180,171,0.2)] hover:scale-105 transition-transform">Accept Load</button>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Fleet Schedule */}
        <div className="bg-[#1B1212] border border-[#ffb4ab]/10 rounded-[2rem] p-8 shadow-2xl relative flex flex-col justify-between">
           <div>
              <div className="flex justify-between items-center mb-8">
                <h3 className="font-['Manrope'] text-xl font-bold text-white flex items-center gap-2">
                  <Calendar className="w-5 h-5 text-[#ffb4ab]" /> Fleet Availability
                </h3>
              </div>
              
               <div className="space-y-5">
                 <div className="flex justify-between items-center">
                    <div className="flex items-center gap-3">
                      <div className="w-3 h-3 rounded-full bg-emerald-400 animate-pulse"></div>
                      <p className="text-sm font-bold text-white">Truck #88 (Reefer)</p>
                    </div>
                    <span className="text-xs text-[#d3c2c1]">Available Now</span>
                 </div>
                 <div className="flex justify-between items-center">
                    <div className="flex items-center gap-3">
                      <div className="w-3 h-3 rounded-full bg-amber-400"></div>
                      <p className="text-sm font-bold text-white">Truck #12 (Dry Van)</p>
                    </div>
                    <span className="text-xs text-[#d3c2c1]">Assigned (ETA 2h)</span>
                 </div>
                 <div className="flex justify-between items-center opacity-50">
                    <div className="flex items-center gap-3">
                      <div className="w-3 h-3 rounded-full bg-red-400"></div>
                      <p className="text-sm font-bold text-white">Truck #04 (Flatbed)</p>
                    </div>
                    <span className="text-xs text-[#d3c2c1]">Maintenance</span>
                 </div>
               </div>
           </div>

           <button className="w-full mt-10 py-4 bg-[#2a1b1b] border border-[#ffb4ab]/20 rounded-2xl text-white font-bold text-sm tracking-wide hover:bg-[#ffb4ab]/10 transition-colors">
             Manage Fleet Assets
           </button>
        </div>

      </div>
    </div>
  );
}
