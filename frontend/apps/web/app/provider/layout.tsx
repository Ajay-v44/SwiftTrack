"use client"

import React, { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/useAuthStore';
import { ProviderSidebar, ProviderHeader } from '@/components/provider/ProviderComponents';

export default function ProviderLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const router = useRouter();
  const { user, isLoading } = useAuthStore();

  useEffect(() => {
    if (!isLoading) {
      if (!user) {
        router.replace('/login');
        return;
      }

      if (user.type !== "PROVIDER_USER") {
        if (user.type?.startsWith("TENANT_") && user.type !== "TENANT_DRIVER") router.replace("/tenant/dashboard")
        else if (user.type === "TENANT_DRIVER" || user.type === "DRIVER_USER") router.replace("/driver/dashboard")
        else if (user.type === "SUPER_ADMIN" || user.type === "SYSTEM_ADMIN") router.replace("/admin/dashboard")
        else if (user.type === "CONSUMER") router.replace("/track")
        else router.replace("/")
      }
    }
  }, [user, isLoading, router]);

  if (isLoading || !user || user.type !== "PROVIDER_USER") {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-50 dark:bg-slate-950">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="bg-slate-50 dark:bg-slate-950 text-slate-900 dark:text-slate-100 font-sans antialiased min-h-screen">
      <ProviderSidebar />
      <main className="ml-72 min-h-screen flex flex-col">
        <ProviderHeader />
        {children}
      </main>
    </div>
  );
}
