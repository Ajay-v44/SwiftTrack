import React from 'react';
import TenantSidebar from '@/components/tenant/TenantSidebar';
import TenantHeader from '@/components/tenant/TenantHeader';

export default function TenantLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="bg-[#0b1326] text-[#dae2fd] font-['Inter'] antialiased min-h-screen">
      <TenantSidebar />
      <main className="ml-72 min-h-screen flex flex-col">
        <TenantHeader />
        {children}
      </main>
    </div>
  );
}
