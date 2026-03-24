import React from 'react';
import { DriverHeader, DriverBottomNav } from '@/components/driver/DriverComponents';

export default function DriverLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="bg-[#020617] text-slate-300 font-['Inter'] antialiased min-h-[100dvh] relative overflow-hidden flex flex-col pt-20 pb-24 touch-pan-y">
      <DriverHeader />
      <main className="flex-1 w-full flex flex-col overflow-y-auto hidden-scrollbar pb-10 shadow-inner">
        {children}
      </main>
      <DriverBottomNav />
    </div>
  );
}
