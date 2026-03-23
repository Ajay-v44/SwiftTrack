"use client";

import React, { useState } from 'react';
import Link from 'next/link';
import { 
  PlusCircle, Filter, Download, PackageSearch, PackageOpen, AlertTriangle, MapPin, Search
} from 'lucide-react';
import { useAuthStore } from '@/store/useAuthStore';

const MOCK_ORDERS = [
  { id: '#TRK-49201A', dest: 'Dallas, TX', operator: 'DHL Express', status: 'In Transit', time: '14:30 EST', eta: 'Oct 25, 2024' },
  { id: '#TRK-49202B', dest: 'Seattle, WA', operator: 'Internal Fleet', status: 'Delivered', time: '09:15 PST', eta: 'Oct 24, 2024' },
  { id: '#TRK-49203C', dest: 'Miami, FL', operator: 'FedEx', status: 'Exception', time: '11:45 EST', eta: 'Delayed' },
  { id: '#TRK-49204D', dest: 'New York, NY', operator: 'Local Express', status: 'Processing', time: '16:00 EST', eta: 'Oct 26, 2024' },
  { id: '#TRK-49205E', dest: 'Chicago, IL', operator: 'DHL Express', status: 'In Transit', time: '10:20 CST', eta: 'Oct 25, 2024' },
];

export default function TenantOrdersPage() {
  const [searchTerm, setSearchTerm] = useState('');

  return (
    <div className="p-12 space-y-8 text-[#dae2fd]">
      
      {/* Header Overview */}
      <div className="flex flex-col md:flex-row gap-6 mb-12">
        <div className="md:w-2/3 bg-gradient-to-r from-[#171f33] to-[#131b2e] rounded-[2rem] p-10 border border-[#3e5bf2]/20 relative overflow-hidden flex flex-col justify-center">
          <div className="absolute right-0 top-0 w-64 h-full bg-[url('data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI0MCIgaGVpZ2h0PSI0MCI+ICAgIDxwYXRoIGQ9Ik0wIDEwaDQwdi0xSDB6bTAgMjBoNDB2LTFIMHoiIHN0cm9rZT0iIzNlNTVmMiIgZmlsbD0ibm9uZSIgb3BhY2l0eT0iLjIiLz48L3N2Zz4=')] mix-blend-overlay"></div>
          
          <div className="flex justify-between items-start z-10 relative">
            <div>
              <h1 className="font-['Manrope'] text-4xl font-extrabold text-white mb-2">Order Command</h1>
              <p className="text-[#c5c5d8]">Monitor and manage your active logistics network.</p>
            </div>
            <Link href="/tenant/orders/create">
              <button className="px-6 py-3 bg-[#3e5bf2] text-white font-bold rounded-full hover:bg-[#2d4ce4] transition-all flex items-center gap-2 shadow-lg shadow-[#3e5bf2]/30 active:scale-95">
                <PlusCircle className="w-5 h-5" /> Create Order
              </button>
            </Link>
          </div>
        </div>

        <div className="md:w-1/3 grid grid-cols-2 gap-4">
          <div className="bg-[#131b2e] rounded-2xl p-6 border border-white/5 flex flex-col justify-center items-center text-center shadow-lg group hover:border-[#3e5bf2]/50 transition-colors">
            <PackageOpen className="w-8 h-8 text-[#00dce5] mb-2 group-hover:scale-110 transition-transform" />
            <p className="font-['Manrope'] text-3xl font-extrabold text-white">543</p>
            <p className="text-[10px] font-bold uppercase tracking-widest text-[#c5c5d8] mt-1">Processed</p>
          </div>
          
          <div className="bg-[#131b2e] rounded-2xl p-6 border border-white/5 flex flex-col justify-center items-center text-center shadow-lg group hover:border-[#ffb4ab]/50 transition-colors">
            <AlertTriangle className="w-8 h-8 text-[#ffb4ab] mb-2 group-hover:scale-110 transition-transform" />
            <p className="font-['Manrope'] text-3xl font-extrabold text-[#ffb4ab]">12</p>
            <p className="text-[10px] font-bold uppercase tracking-widest text-[#c5c5d8] mt-1">Open Issues</p>
          </div>
        </div>
      </div>

      {/* Main Content Area */}
      <div className="bg-[#131b2e] border border-white/5 rounded-[2rem] p-8 shadow-xl">
        
        {/* Controls */}
        <div className="flex flex-col lg:flex-row justify-between items-start gap-4 mb-8">
          <div className="relative w-full max-w-md group">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-[#c5c5d8] w-5 h-5" />
            <input
              className="w-full bg-[#171f33] border border-white/5 rounded-full py-3 pl-12 pr-4 text-sm focus:ring-2 focus:ring-[#3e5bf2]/50 text-[#dae2fd] placeholder:text-[#c5c5d8]/50 outline-none transition-all"
              placeholder="Search by ID, destination, or operator..."
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          <div className="flex gap-3">
            <button className="px-5 py-3 bg-[#2d3449] border border-[#444655] rounded-full text-xs font-bold text-white flex items-center gap-2 hover:bg-[#3e495d] transition-colors">
              <Filter className="w-4 h-4"/> Filter Status
            </button>
            <button className="px-5 py-3 bg-[#171f33] border border-[#3e5bf2]/30 rounded-full text-xs font-bold text-[#bac3ff] flex items-center gap-2 hover:bg-[#3e5bf2] hover:text-white transition-colors group">
              <Download className="w-4 h-4 group-hover:-translate-y-0.5 transition-transform"/> Export Log
            </button>
          </div>
        </div>

        {/* Data Table */}
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse min-w-[800px]">
            <thead>
              <tr className="border-b border-[#2d3449]/50">
                <th className="pb-4 pt-2 px-4 text-xs font-bold uppercase tracking-wider text-[#8e8fa1]">Tracking ID</th>
                <th className="pb-4 pt-2 px-4 text-xs font-bold uppercase tracking-wider text-[#8e8fa1]">Destination</th>
                <th className="pb-4 pt-2 px-4 text-xs font-bold uppercase tracking-wider text-[#8e8fa1]">Operator</th>
                <th className="pb-4 pt-2 px-4 text-xs font-bold uppercase tracking-wider text-[#8e8fa1]">Status</th>
                <th className="pb-4 pt-2 px-4 text-xs font-bold uppercase tracking-wider text-[#8e8fa1]">Last Update</th>
                <th className="pb-4 pt-2 px-4 text-xs font-bold uppercase tracking-wider text-[#8e8fa1]">ETA</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#2d3449]/30">
              {MOCK_ORDERS.filter(o => o.id.includes(searchTerm) || o.dest.includes(searchTerm)).map((order, idx) => (
                <tr key={idx} className="hover:bg-[#171f33]/80 transition-colors group cursor-pointer">
                  <td className="py-5 px-4">
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full bg-[#3e5bf2]/10 flex items-center justify-center text-[#3e5bf2]">
                        <PackageSearch className="w-4 h-4" />
                      </div>
                      <span className="font-bold text-white text-sm">{order.id}</span>
                    </div>
                  </td>
                  <td className="py-5 px-4">
                    <div className="flex items-center gap-2 text-sm text-[#c5c5d8]">
                      <MapPin className="w-4 h-4 text-[#8e8fa1]" />
                      {order.dest}
                    </div>
                  </td>
                  <td className="py-5 px-4 text-sm font-medium text-[#bac3ff]">{order.operator}</td>
                  <td className="py-5 px-4">
                    <span className={`px-3 py-1 text-[10px] font-bold rounded-full border flex items-center w-max gap-1.5
                      ${order.status === 'In Transit' ? 'bg-[#3e5bf2]/10 text-[#3e5bf2] border-[#3e5bf2]/20' : ''}
                      ${order.status === 'Delivered' ? 'bg-[#00dce5]/10 text-[#00dce5] border-[#00dce5]/20' : ''}
                      ${order.status === 'Exception' ? 'bg-[#ffb4ab]/10 text-[#ffb4ab] border-[#ffb4ab]/20' : ''}
                      ${order.status === 'Processing' ? 'bg-[#c5c5d8]/10 text-[#c5c5d8] border-[#c5c5d8]/20' : ''}
                    `}>
                      {order.status === 'In Transit' && <span className="w-1.5 h-1.5 rounded-full bg-[#3e5bf2] animate-pulse"></span>}
                      {order.status === 'Delivered' && <span className="w-1.5 h-1.5 rounded-full bg-[#00dce5]"></span>}
                      {order.status === 'Exception' && <span className="w-1.5 h-1.5 rounded-full bg-[#ffb4ab] animate-pulse"></span>}
                      {order.status === 'Processing' && <span className="w-1.5 h-1.5 rounded-full bg-[#c5c5d8]"></span>}
                      {order.status}
                    </span>
                  </td>
                  <td className="py-5 px-4 text-sm text-[#8e8fa1]">{order.time}</td>
                  <td className="py-5 px-4 text-sm font-bold text-white">{order.eta}</td>
                </tr>
              ))}
            </tbody>
          </table>
          
          {/* Pagination */}
          <div className="mt-8 pt-6 border-t border-[#2d3449]/50 flex justify-between items-center text-[#c5c5d8] text-xs">
            <span>Showing 1 to 5 of 543 entries</span>
            <div className="flex gap-2">
              <button className="px-3 py-1.5 rounded-md bg-[#2d3449] hover:bg-[#3e495d] transition">Prev</button>
              <button className="px-3 py-1.5 rounded-md bg-[#3e5bf2] text-white font-bold shadow-md">1</button>
              <button className="px-3 py-1.5 rounded-md bg-[#171f33] hover:bg-[#2d3449] transition">2</button>
              <button className="px-3 py-1.5 rounded-md bg-[#171f33] hover:bg-[#2d3449] transition">3</button>
              <span className="px-2">...</span>
              <button className="px-3 py-1.5 rounded-md bg-[#2d3449] hover:bg-[#3e495d] transition">Next</button>
            </div>
          </div>

        </div>
      </div>
    </div>
  );
}
