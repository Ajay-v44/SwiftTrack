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
    { name: 'Dashboard', href: '/provider/dashboard', icon: Map },
    { name: 'Wallet', href: '/provider/wallet', icon: FileText },
  ];

  return (
    <aside className="fixed left-0 top-0 h-full flex flex-col py-8 bg-white dark:bg-slate-900 w-72 border-r border-slate-200 dark:border-slate-800 z-50 shadow-sm">
      <div className="px-10 mb-12">
        <span className="text-2xl font-black tracking-tighter bg-gradient-to-r from-blue-600 to-indigo-500 bg-clip-text text-transparent">
          SwiftTrack ho
        </span>
        <p className="text-[10px] uppercase tracking-widest text-slate-500 dark:text-slate-400 mt-1 font-bold">
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
              className={`px-6 py-3.5 rounded-2xl flex items-center gap-4 transition-all duration-300 ease-in-out font-medium text-sm ${isActive
                  ? 'text-white font-bold bg-gradient-to-r from-blue-600 to-indigo-600 shadow-md shadow-blue-500/20'
                  : 'text-slate-600 dark:text-slate-400 hover:bg-blue-50 dark:hover:bg-slate-800 hover:text-blue-600 dark:hover:text-blue-400'
                }`}
            >
              <item.icon className="w-5 h-5" />
              <span>{item.name}</span>
            </Link>
          );
        })}
      </nav>

      <div className="px-8 mt-6">
        <div className="bg-slate-50 dark:bg-slate-950 p-4 rounded-3xl flex items-center gap-3 border border-slate-200 dark:border-slate-800">
          <div className="w-10 h-10 rounded-full bg-blue-100 dark:bg-blue-900 flex items-center justify-center text-blue-700 dark:text-blue-300 font-bold shrink-0">
            PR
          </div>
          <div className="overflow-hidden">
            <p className="text-xs font-bold text-slate-900 dark:text-white truncate">{user?.name || 'Logistics Partner'}</p>
            <p className="text-[10px] text-slate-500 dark:text-slate-400 truncate tracking-wider">{user?.type?.replace('_', ' ') || 'PROVIDER USER'}</p>
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
    <header className="flex justify-between items-center px-12 h-20 sticky top-0 z-40 bg-white/80 dark:bg-slate-900/80 backdrop-blur-[24px] border-b border-slate-200 dark:border-slate-800">
      <div className="flex items-center gap-8 w-1/2">
        <div className="relative w-full max-w-md group">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 w-4 h-4" />
          <input
            className="w-full bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-full py-2.5 pl-12 pr-4 text-sm font-medium focus:ring-2 focus:ring-blue-500 text-slate-900 dark:text-white placeholder:text-slate-400 outline-none transition-all block"
            placeholder="Search manifests, vehicles, drivers..."
            type="text"
          />
        </div>
      </div>

      <div className="flex items-center gap-4">
        <div className="flex items-center gap-2 px-4 py-1.5 rounded-full bg-amber-50 dark:bg-amber-500/10 border border-amber-200 dark:border-amber-500/20 text-amber-600 dark:text-amber-400 text-xs font-bold">
          <Clock className="w-4 h-4" /> 12 Pending Bids
        </div>

        <button className="w-10 h-10 flex items-center justify-center rounded-xl text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors relative">
          <Bell className="w-5 h-5" />
          <span className="absolute top-2 right-2.5 w-2 h-2 bg-blue-500 rounded-full border-2 border-white dark:border-slate-900"></span>
        </button>

        <div className="h-6 w-[1px] bg-slate-200 dark:bg-slate-800 mx-2"></div>

        <button
          onClick={handleLogout}
          className="w-10 h-10 flex items-center justify-center rounded-xl text-slate-500 dark:text-slate-400 hover:bg-rose-50 hover:text-rose-600 dark:hover:bg-rose-500/10 dark:hover:text-rose-400 transition-colors"
        >
          <LogOut className="w-5 h-5" />
        </button>
      </div>
    </header>
  );
}
