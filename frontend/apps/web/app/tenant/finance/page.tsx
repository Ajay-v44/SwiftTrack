"use client";

import React, { useEffect, useState } from 'react';
import { 
  Radio, Wallet, ArrowRightLeft, Target, 
  FileText, TrendingUp, DollarSign, Download, Settings, Filter, FileSpreadsheet
} from 'lucide-react';
import { useAuthStore } from '@/store/useAuthStore';
import apiClient from '@/lib/api-client';

const MOCK_TRANSACTIONS = [
  { id: '#ST-10294-X', desc: 'Order #1023 Delivery Fee', date: 'Oct 24, 2024', amount: -142.00, status: 'Success' },
  { id: '#ST-10301-T', desc: 'Wallet Top-up', date: 'Oct 23, 2024', amount: 5000.00, status: 'Success' },
  { id: '#ST-10305-B', desc: 'Bulk Order #1045 Fuel Surcharge', date: 'Oct 22, 2024', amount: -89.50, status: 'Pending' },
  { id: '#ST-10312-Z', desc: 'Warehouse Storage Fee - Q3', date: 'Oct 20, 2024', amount: -1240.00, status: 'Success' },
  { id: '#ST-10319-M', desc: 'Refund: Cancelled Shipment #992', date: 'Oct 19, 2024', amount: 45.00, status: 'Success' },
];

export default function TenantFinancePage() {
  const { user } = useAuthStore();
  const [balance, setBalance] = useState<number>(42890.50);

  useEffect(() => {
    async function fetchFinanceData() {
      try {
        if (!user?.id) return;
        const response = await apiClient.get('/api/accounts/v1/getMyAccount', {
          params: { userId: user.id }
        });
        if (response.data?.balance !== undefined) {
          setBalance(response.data.balance);
        }
      } catch (error) {
        console.error("Finance data fetch error:", error);
      }
    }
    fetchFinanceData();
  }, [user]);

  return (
    <div className="p-12 space-y-8 text-[#dae2fd]">
      
      {/* Header */}
      <div className="flex justify-between items-center bg-[#171f33] p-4 rounded-full border border-white/5">
        <div className="relative w-full max-w-lg ml-2">
          <input
            className="w-full bg-[#2d3449] border-none rounded-full py-2.5 pl-6 pr-4 text-sm focus:ring-2 focus:ring-[#3e5bf2]/30 text-[#dae2fd] placeholder:text-[#c5c5d8]/50 outline-none"
            placeholder="Search transactions, invoices..."
            type="text"
          />
        </div>
        <div className="flex items-center gap-4 px-4 text-[#3e5bf2] text-sm font-bold bg-[#3e5bf2]/10 py-2 rounded-full">
          Finance Hub <CheckCircleIcon className="w-4 h-4" />
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Main Wallet Card */}
        <div className="lg:col-span-2 bg-gradient-to-br from-[#131b2e] to-[#1a2542] rounded-[2rem] p-10 border border-[#3e5bf2]/20 relative overflow-hidden shadow-2xl">
          <div className="absolute top-0 right-0 w-64 h-64 bg-[#3e5bf2]/20 rounded-full blur-[80px] -z-10"></div>
          
          <div className="flex justify-between items-start mb-12">
            <div>
              <p className="text-[10px] font-bold uppercase tracking-widest text-[#bac3ff] mb-2">Available Credits</p>
              <h1 className="font-['Manrope'] text-6xl font-extrabold text-white">
                ${balance.toLocaleString('en-US', { minimumFractionDigits: 2 })}
              </h1>
            </div>
            <div className="px-4 py-2 bg-[#00dce5]/10 border border-[#00dce5]/30 rounded-full flex items-center gap-2 text-[#00dce5] text-xs font-bold animate-pulse">
              <Radio className="w-4 h-4" /> LIVE UPDATES
            </div>
          </div>

          <div className="flex gap-4">
            <button className="px-8 py-4 rounded-full bg-[#3e5bf2] hover:bg-[#2d4ce4] text-white font-bold transition-all shadow-lg flex items-center gap-2">
              <Wallet className="w-4 h-4" /> Top-up Wallet
            </button>
            <button className="px-8 py-4 rounded-full bg-[#2d3449] hover:bg-[#3e495d] text-[#c5c5d8] hover:text-white font-bold transition-colors border border-white/5">
              Transfer Funds
            </button>
          </div>
        </div>

        {/* Reconciliation Widget */}
        <div className="bg-[#171f33] rounded-[2rem] p-8 border border-white/5 shadow-xl flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-4 mb-6">
              <div className="w-12 h-12 rounded-xl bg-[#3e5bf2]/20 text-[#3e5bf2] flex items-center justify-center">
                <Target className="w-6 h-6" />
              </div>
              <div>
                <h3 className="font-['Manrope'] text-lg font-bold text-white">Reconciliation</h3>
                <p className="text-xs text-[#c5c5d8]">Review monthly statements</p>
              </div>
            </div>

            <div className="bg-[#131b2e] rounded-xl p-4 border border-white/5 mb-6">
              <div className="flex justify-between items-center mb-2">
                <p className="text-sm font-bold text-white">March 2024</p>
                <span className="text-[10px] uppercase font-bold text-emerald-400 bg-emerald-400/10 px-2 py-0.5 rounded-full">Ready</span>
              </div>
              <p className="text-xs text-[#c5c5d8] flex items-center justify-between">
                <span className="flex items-center gap-1"><FileText className="w-3 h-3"/> ST_Invoice_MAR.pdf</span>
                <span className="text-white font-bold cursor-pointer hover:underline">Download</span>
              </p>
            </div>

            <div className="flex justify-between items-center text-sm mb-2">
              <span className="text-[#c5c5d8]">Pending Invoices</span>
              <span className="font-bold text-white">02</span>
            </div>
            <div className="flex justify-between items-center text-sm mb-4">
              <span className="text-[#c5c5d8]">Auto-reconcile status</span>
              <span className="font-bold text-[#00dce5]">Enabled</span>
            </div>
            
            <div className="w-full bg-[#2d3449] h-1.5 rounded-full overflow-hidden mb-2">
              <div className="bg-[#3e5bf2] w-[85%] h-full"></div>
            </div>
            <p className="text-[10px] text-[#8e8fa1] italic text-right">85% of transactions auto-matched</p>
          </div>
          
          <button className="w-full py-4 mt-6 bg-[#2d3449]/50 hover:bg-[#2d3449] text-white font-bold rounded-xl text-sm transition-colors border border-white/5">
            View Full History
          </button>
        </div>
      </div>

      {/* Transaction History Data Table */}
      <div className="bg-[#131b2e] rounded-[2rem] p-8 border border-white/5 shadow-xl">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h2 className="font-['Manrope'] text-2xl font-bold text-white">Transaction History</h2>
            <p className="text-xs text-[#c5c5d8] mt-1">Detailed breakdown of all financial activities</p>
          </div>
          <div className="flex gap-2">
            <button className="px-4 py-2 bg-[#2d3449] rounded-full text-xs font-bold text-white flex items-center gap-2 hover:bg-[#3e495d] transition-colors"><Filter className="w-3 h-3"/> Filter</button>
            <button className="px-4 py-2 bg-[#2d3449] rounded-full text-xs font-bold text-white flex items-center gap-2 hover:bg-[#3e495d] transition-colors"><Download className="w-3 h-3"/> Export</button>
          </div>
        </div>

        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="border-b border-[#2d3449]">
              <th className="pb-4 pt-2 text-xs font-bold uppercase tracking-wider text-[#c5c5d8]">Transaction ID</th>
              <th className="pb-4 pt-2 text-xs font-bold uppercase tracking-wider text-[#c5c5d8]">Description</th>
              <th className="pb-4 pt-2 text-xs font-bold uppercase tracking-wider text-[#c5c5d8]">Date</th>
              <th className="pb-4 pt-2 text-xs font-bold uppercase tracking-wider text-[#c5c5d8]">Amount</th>
              <th className="pb-4 pt-2 text-xs font-bold uppercase tracking-wider text-[#c5c5d8]">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[#2d3449]/50">
            {MOCK_TRANSACTIONS.map((txn, idx) => (
              <tr key={idx} className="hover:bg-[#171f33] transition-colors group">
                <td className="py-5 text-sm font-medium text-[#c5c5d8] italic">{txn.id}</td>
                <td className="py-5">
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-full bg-[#3e5bf2]/20 flex items-center justify-center text-[#3e5bf2]">
                      <FileSpreadsheet className="w-4 h-4"/>
                    </div>
                    <span className="text-sm font-bold text-white">{txn.desc}</span>
                  </div>
                </td>
                <td className="py-5 text-sm text-[#c5c5d8]">{txn.date}</td>
                <td className="py-5 text-sm font-extrabold">
                  <span className={txn.amount > 0 ? "text-[#00dce5]" : "text-[#ffb4ab]"}>
                    {txn.amount > 0 ? '+' : ''} ${Math.abs(txn.amount).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                  </span>
                </td>
                <td className="py-5">
                  <span className={`px-3 py-1 text-[10px] font-bold rounded-full border ${txn.status === 'Success' ? 'bg-[#00dce5]/10 text-[#00dce5] border-[#00dce5]/20' : 'bg-[#c5c5d8]/10 text-[#c5c5d8] border-[#c5c5d8]/20'}`}>
                    {txn.status}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        <div className="mt-6 flex justify-between items-center text-[#c5c5d8] text-xs">
          <span>Showing 5 of 1,240 transactions</span>
          <div className="flex gap-2">
            <button className="w-8 h-8 rounded-full bg-[#3e5bf2] text-white flex items-center justify-center font-bold">1</button>
            <button className="w-8 h-8 rounded-full bg-[#2d3449] hover:bg-[#3e495d] transition flex items-center justify-center font-bold">2</button>
            <button className="w-8 h-8 rounded-full bg-[#2d3449] hover:bg-[#3e495d] transition flex items-center justify-center font-bold">3</button>
            <span className="px-2 self-end">...</span>
          </div>
        </div>
      </div>

      {/* Footer Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div className="bg-[#131b2e] rounded-2xl p-6 border border-white/5 flex items-center gap-6">
          <div className="w-12 h-12 rounded-full bg-[#3e5bf2]/10 flex items-center justify-center text-[#3e5bf2]">
            <TrendingUp className="w-6 h-6" />
          </div>
          <div>
            <p className="text-[10px] font-bold uppercase tracking-widest text-[#c5c5d8]">Weekly Spend</p>
            <p className="font-['Manrope'] text-2xl font-extrabold text-white">$4,231.00</p>
          </div>
        </div>
        
        <div className="bg-[#131b2e] rounded-2xl p-6 border border-white/5 flex items-center gap-6">
          <div className="w-12 h-12 rounded-full bg-[#00dce5]/10 flex items-center justify-center text-[#00dce5]">
            <DollarSign className="w-6 h-6" />
          </div>
          <div>
            <p className="text-[10px] font-bold uppercase tracking-widest text-[#c5c5d8]">Cost Savings</p>
            <p className="font-['Manrope'] text-2xl font-extrabold text-white">$812.20</p>
          </div>
        </div>
        
        <div className="bg-[#131b2e] rounded-2xl p-6 border border-white/5 flex items-center gap-6">
          <div className="w-12 h-12 rounded-full bg-[#ffb4ab]/10 flex items-center justify-center text-[#ffb4ab]">
            <ArrowRightLeft className="w-6 h-6" />
          </div>
          <div>
            <p className="text-[10px] font-bold uppercase tracking-widest text-[#c5c5d8]">Unpaid Dues</p>
            <p className="font-['Manrope'] text-2xl font-extrabold text-white">$0.00</p>
          </div>
        </div>

        <div className="bg-[#131b2e] rounded-2xl p-6 border border-[#3e5bf2]/20 flex items-center gap-6 shadow-[0_0_15px_rgba(62,91,242,0.15)] overflow-hidden relative">
          <div className="absolute right-0 top-0 w-32 h-32 bg-[#3e5bf2]/20 blur-[40px] -z-10"></div>
          <div className="w-12 h-12 rounded-full bg-[#3e5bf2] flex items-center justify-center text-white shadow-lg shadow-[#3e5bf2]/40">
            <FileText className="w-6 h-6" />
          </div>
          <div>
            <p className="text-[10px] font-bold uppercase tracking-widest text-[#bac3ff]">Invoices</p>
            <p className="font-['Manrope'] text-2xl font-extrabold text-white">12 Total</p>
          </div>
        </div>
      </div>
    </div>
  );
}

function CheckCircleIcon({ className }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path d="M12 2C6.477 2 2 6.477 2 12C2 17.523 6.477 22 12 22C17.523 22 22 17.523 22 12C22 6.477 17.523 2 12 2ZM10.5 16.5L6.5 12.5L7.914 11.086L10.5 13.672L16.086 8.086L17.5 9.5L10.5 16.5Z" fill="currentColor"/>
    </svg>
  );
}
