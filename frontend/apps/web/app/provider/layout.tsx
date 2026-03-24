import React from 'react';
import { ProviderSidebar, ProviderHeader } from '@/components/provider/ProviderComponents';

export default function ProviderLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="bg-[#120B0B] text-[#d3c2c1] font-['Inter'] antialiased min-h-screen">
      <ProviderSidebar />
      <main className="ml-72 min-h-screen flex flex-col">
        <ProviderHeader />
        {children}
      </main>
    </div>
  );
}
