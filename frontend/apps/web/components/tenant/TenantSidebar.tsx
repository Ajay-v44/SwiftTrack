"use client";

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { LayoutDashboard, Package, Wallet, Truck, Users, Puzzle, Settings } from 'lucide-react';
import { useAuthStore } from '@/store/useAuthStore';

export default function TenantSidebar() {
  const pathname = usePathname();
  const { user } = useAuthStore();

  const navigation = [
    { name: 'Dashboard', href: '/tenant/dashboard', icon: LayoutDashboard },
    { name: 'Orders', href: '/tenant/orders', icon: Package },
    { name: 'Finance', href: '/tenant/finance', icon: Wallet },
    { name: 'Delivery Settings', href: '/tenant/settings/delivery', icon: Truck },
    { name: 'Team Management', href: '/tenant/team', icon: Users },
    { name: 'Integration', href: '/tenant/integrations', icon: Puzzle },
  ];

  const getInitials = (name?: string) => {
    if (!name) return 'U';
    return name.split(' ').map((n) => n[0]).join('').substring(0, 2).toUpperCase();
  };

  return (
    <aside className="fixed left-0 top-0 h-full flex flex-col py-8 bg-[#171f33] w-72 rounded-r-[3rem] overflow-hidden shadow-[0px_24px_48px_-12px_rgba(0,46,202,0.08)] z-50">
      <div className="px-10 mb-12">
        <span className="text-2xl font-black tracking-tighter text-white bg-gradient-to-r from-[#3e5bf2] to-[#00dce5] bg-clip-text text-transparent">
          SwiftTrack
        </span>
        <p className="text-[10px] uppercase tracking-widest text-[#c5c5d8]/50 mt-1 font-bold">
          Command Center
        </p>
      </div>

      <nav className="flex-1 flex flex-col gap-1 overflow-y-auto hidden-scrollbar">
        {navigation.map((item) => {
          const isActive = pathname.startsWith(item.href);
          return (
            <Link
              key={item.name}
              href={item.href}
              className={`px-10 py-3 flex items-center gap-4 transition-all duration-300 ease-[cubic-bezier(0.4,0,0.2,1)] active:scale-[0.97] font-['Manrope'] font-medium text-sm ${
                isActive
                  ? 'text-[#bac3ff] font-bold bg-[#2d3449] rounded-full mx-4 px-6'
                  : 'text-[#c5c5d8] hover:bg-[#2d3449] hover:text-[#3e5bf2]'
              }`}
            >
              <item.icon className="w-5 h-5" />
              <span>{item.name}</span>
            </Link>
          );
        })}

        <div className="mt-auto pt-4">
          <Link
            href="/tenant/settings"
            className={`px-10 py-3 flex items-center gap-4 transition-all duration-300 ease-[cubic-bezier(0.4,0,0.2,1)] active:scale-[0.97] font-['Manrope'] font-medium text-sm ${
              pathname === '/tenant/settings'
                ? 'text-[#bac3ff] font-bold bg-[#2d3449] rounded-full mx-4 px-6'
                : 'text-[#c5c5d8] hover:bg-[#2d3449] hover:text-[#3e5bf2]'
            }`}
          >
            <Settings className="w-5 h-5" />
            <span>Settings &amp; Support</span>
          </Link>
        </div>
      </nav>

      {/* User Profile Mini Banner */}
      <div className="px-8 mt-6">
        <div className="bg-[#2d3449]/40 p-4 rounded-xl flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-[#3e5bf2] flex items-center justify-center text-white font-bold shrink-0">
            {getInitials(user?.name)}
          </div>
          <div className="overflow-hidden">
            <p className="text-xs font-bold text-white truncate">{user?.name || 'Loading User..'}</p>
            <p className="text-[10px] text-[#c5c5d8] truncate">{user?.type?.replace('_', ' ') || 'TENANT ADMIN'}</p>
          </div>
        </div>
      </div>
    </aside>
  );
}
