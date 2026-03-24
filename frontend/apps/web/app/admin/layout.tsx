import React from 'react';
import { AdminSidebar, AdminHeader } from '@/components/admin/AdminComponents';

export default function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="bg-[#050810] text-[#c5c5d8] font-['Inter'] antialiased min-h-screen">
      <AdminSidebar />
      <main className="ml-72 min-h-screen flex flex-col">
        <AdminHeader />
        {children}
      </main>
    </div>
  );
}
