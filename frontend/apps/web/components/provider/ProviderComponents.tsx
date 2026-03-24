"use client";

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Truck, Map, Briefcase, FileText, Settings, Search, Bell, LogOut, CheckCircle, Clock } from 'lucide-react';
import { useAuthStore } from '@/store/useAuthStore';
import { useRouter } from 'next/navigation';

export function ProviderSidebar() {
  const pathname = usePathname();
  const { user } = useAuthStore();

  const navigation = [
    { name: 'Dispatch Board', href: '/provider/dashboard', icon: Map },
    { name: 'Active Routes', href: '/provider/routes', icon: Truck },
    { name: 'Fleet Status', href: '/provider/fleet', icon: Briefcase },
    { name: 'Settlements', href: '/provider/settlements', icon: FileText },
    { name: 'API Connect', href: '/provider/api', icon: Settings },
  ];

  return (
    <aside className="fixed left-0 top-0 h-full flex flex-col py-8 bg-[#1B1212] w-72 border-r border-[#ffb4ab]/10 z-50">
      <div className="px-10 mb-12">
        <span className="text-2xl font-black tracking-tighter text-white bg-gradient-to-r from-[#ffb4ab] to-amber-400 bg-clip-text text-transparent">
          SwiftTrack Prov
        </span>
        <p className="text-[10px] uppercase tracking-widest text-[#ffb4ab]/60 mt-1 font-bold">
          Partner Portal
        </p>
      </div>

      <nav className="flex-1 flex flex-col gap-1 overflow-y-auto hidden-scrollbar px-4">
        {navigation.map((item) => {
          const isActive = pathname?.startsWith(item.href);
          return (
            <Link
              key={item.name}
              href={item.href}
              className={`px-6 py-3.5 rounded-2xl flex items-center gap-4 transition-all duration-300 ease-in-out font-['Manrope'] font-medium text-sm ${
                isActive
                  ? 'text-[#410002] font-black bg-gradient-to-r from-[#ffb4ab] to-amber-200 shadow-xl'
                  : 'text-[#d3c2c1] hover:bg-[#ffb4ab]/10 hover:text-[#ffb4ab]'
              }`}
            >
              <item.icon className="w-5 h-5" />
              <span>{item.name}</span>
            </Link>
          );
        })}
      </nav>

      <div className="px-8 mt-6">
        <div className="bg-[#2a1b1b] p-4 rounded-3xl flex items-center gap-3 border border-[#ffb4ab]/10">
          <div className="w-10 h-10 rounded-full bg-[#ffb4ab] flex items-center justify-center text-[#410002] font-bold shrink-0 shadow-inner">
            PR
          </div>
          <div className="overflow-hidden">
            <p className="text-xs font-bold text-white truncate">{user?.name || 'Logistics Partner'}</p>
            <p className="text-[10px] text-[#ffb4ab]/80 truncate tracking-wider">{user?.type?.replace('_', ' ') || 'PROVIDER ADMIN'}</p>
          </div>
        </div>
      </div>
    </aside>
  );
}

export function ProviderHeader() {
  const { user, logout } = useAuthStore();
  const router = useRouter();

  const handleLogout = () => {
    logout();
    router.push('/login');
  };

  return (
    <header className="flex justify-between items-center px-12 h-20 sticky top-0 z-40 bg-[#1B1212]/80 backdrop-blur-[24px] border-b border-white/5 font-['Manrope']">
      <div className="flex items-center gap-8 w-1/2">
        <div className="relative w-full max-w-md group">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-[#d3c2c1] w-4 h-4" />
          <input
            className="w-full bg-[#2a1b1b] border border-transparent rounded-full py-2.5 pl-12 pr-4 text-sm font-medium focus:ring-2 focus:ring-[#ffb4ab]/40 text-white placeholder:text-[#d3c2c1]/50 outline-none transition-all shadow-inner block"
            placeholder="Search manifests, vehicles, drivers..."
            type="text"
          />
        </div>
      </div>

      <div className="flex items-center gap-4">
        <div className="flex items-center gap-2 px-4 py-1.5 rounded-full bg-amber-500/10 border border-amber-500/20 text-amber-400 text-xs font-bold">
           <Clock className="w-4 h-4" /> 12 Pending Bids
        </div>
        
        <button className="w-10 h-10 flex items-center justify-center rounded-xl text-[#d3c2c1] hover:bg-[#2a1b1b] transition-colors relative">
          <Bell className="w-5 h-5" />
          <span className="absolute top-2 right-2.5 w-2 h-2 bg-[#ffb4ab] rounded-full border-2 border-[#1B1212]"></span>
        </button>

        <div className="h-6 w-[1px] bg-white/10 mx-2"></div>

        <button
          onClick={handleLogout}
          className="w-10 h-10 flex items-center justify-center rounded-xl text-[#d3c2c1] hover:bg-[#ffb4ab]/10 hover:text-[#ffb4ab] transition-colors"
        >
          <LogOut className="w-5 h-5" />
        </button>
      </div>
    </header>
  );
}
