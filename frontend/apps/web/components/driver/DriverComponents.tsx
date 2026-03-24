"use client";

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Navigation, Menu, User, Bell, Map as MapIcon, Calendar, Wallet } from 'lucide-react';
import { useAuthStore } from '@/store/useAuthStore';
import { useRouter } from 'next/navigation';

export function DriverHeader() {
  const { user } = useAuthStore();
  return (
    <header className="fixed top-0 w-full h-20 bg-[#0f172a]/90 backdrop-blur-xl border-b border-indigo-400/20 z-50 flex items-center justify-between px-6 shadow-2xl safe-area-top">
      <div className="flex items-center gap-4">
        <button className="w-12 h-12 bg-indigo-900/50 rounded-full flex items-center justify-center text-indigo-300 active:scale-95 transition-transform">
          <Menu className="w-6 h-6" />
        </button>
        <div>
          <h1 className="text-xl font-black text-white font-['Manrope'] drop-shadow-md">On Duty</h1>
          <p className="text-[10px] text-emerald-400 font-bold uppercase tracking-wider flex items-center gap-1">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span> GPS Active
          </p>
        </div>
      </div>

      <div className="flex items-center gap-4">
        <button className="w-12 h-12 bg-[#1e293b] border border-white/5 rounded-full flex items-center justify-center text-indigo-200 relative">
          <Bell className="w-5 h-5" />
          <span className="absolute top-3 right-3 w-2 h-2 rounded-full bg-amber-400"></span>
        </button>
        <div className="w-12 h-12 rounded-full bg-indigo-500 overflow-hidden border-2 border-[#0f172a] shadow-lg">
           <User className="w-full h-full p-2 text-white opacity-80" />
        </div>
      </div>
    </header>
  );
}

export function DriverBottomNav() {
  const pathname = usePathname();
  
  const nav = [
    { label: 'Active Route', href: '/driver/dashboard', icon: Navigation },
    { label: 'Jobs', href: '/driver/jobs', icon: MapIcon },
    { label: 'Schedule', href: '/driver/schedule', icon: Calendar },
    { label: 'Earnings', href: '/driver/earnings', icon: Wallet },
  ];

  return (
    <nav className="fixed bottom-0 w-full h-24 bg-[#0f172a]/90 backdrop-blur-2xl border-t border-indigo-400/20 z-50 flex items-center justify-around px-4 pb-4 shadow-[0_-20px_40px_rgba(0,0,0,0.5)] safe-area-bottom">
      {nav.map(item => {
        const isActive = pathname === item.href;
        return (
          <Link
            key={item.label}
            href={item.href}
            className={`flex flex-col items-center justify-center gap-1.5 w-full h-full ${isActive ? 'text-indigo-400' : 'text-slate-400'}`}
          >
            <div className={`transition-all duration-300 ${isActive ? 'bg-indigo-500/20 p-3 rounded-2xl shadow-inner border border-indigo-500/30 -translate-y-2' : 'p-2'}`}>
              <item.icon className="w-6 h-6" />
            </div>
            <span className={`text-[10px] font-bold tracking-wide transition-opacity ${isActive ? 'opacity-100 font-black' : 'opacity-70'}`}>
              {item.label}
            </span>
          </Link>
        )
      })}
    </nav>
  );
}
