"use client";

import React from 'react';
import { Search, Bell, Contrast, LogOut } from 'lucide-react';
import { useAuthStore } from '@/store/useAuthStore';
import { useRouter } from 'next/navigation';

export default function TenantHeader() {
  const { user, logout } = useAuthStore();
  const router = useRouter();

  const handleLogout = () => {
    logout();
    router.push('/login');
  };

  return (
    <header className="flex justify-between items-center px-12 h-20 sticky top-0 z-40 bg-[#171f33]/60 backdrop-blur-[24px] border-none bg-gradient-to-b from-[#0b1326] to-transparent font-['Manrope'] text-lg font-bold">
      <div className="flex items-center gap-8 w-1/2">
        <div className="relative w-full max-w-md group">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-[#c5c5d8] w-5 h-5" />
          <input
            className="w-full bg-[#2d3449] border-none rounded-full py-2.5 pl-12 pr-4 text-sm focus:ring-2 focus:ring-[#3e5bf2]/30 text-[#dae2fd] placeholder:text-[#c5c5d8]/50 outline-none"
            placeholder="Search shipments, orders, or tracking ID..."
            type="text"
          />
        </div>
      </div>

      <div className="flex items-center gap-6">
        <button className="w-10 h-10 flex items-center justify-center rounded-full text-[#c5c5d8] hover:bg-[#2d3449]/50 transition-colors relative">
          <Bell className="w-5 h-5" />
          <span className="absolute top-2 right-2 w-2 h-2 bg-[#00dce5] rounded-full"></span>
        </button>
        <button className="w-10 h-10 flex items-center justify-center rounded-full text-[#c5c5d8] hover:bg-[#2d3449]/50 transition-colors">
          <Contrast className="w-5 h-5" />
        </button>

        <div className="h-8 w-[1px] bg-[#444655]/30 mx-2"></div>

        <div className="flex items-center gap-3 relative group">
          <div className="text-right">
            <p className="text-xs font-bold text-[#dae2fd]">{user?.name || 'Administrator'}</p>
            <p className="text-[10px] text-[#c5c5d8]">{user?.roles?.[0] || 'Fleet Manager'}</p>
          </div>
          <div className="w-10 h-10 rounded-full overflow-hidden bg-[#2d3449] border-2 border-[#3e5bf2]/20 relative">
            <img
              className="w-full h-full object-cover"
              alt="User avatar"
              src="https://api.dicebear.com/7.x/initials/svg?seed=Admin&backgroundColor=3e5bf2,2d3449"
            />
          </div>

          <div className="absolute right-0 top-12 scale-0 group-hover:scale-100 transition-all origin-top-right">
            <button
              onClick={handleLogout}
              className="px-4 py-2 mt-2 bg-[#2d3449] rounded-xl text-[#c5c5d8] hover:text-white hover:bg-[#ffb4ab]/10 flex items-center gap-2 text-sm drop-shadow-lg"
            >
              <LogOut className="w-4 h-4" />
              Logout
            </button>
          </div>
        </div>
      </div>
    </header>
  );
}
