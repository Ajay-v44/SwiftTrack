import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { Toaster } from "@/components/ui/sonner";
import { AppShell } from "@/components/AppShell";
import { NotificationManager } from "@/components/NotificationManager";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "SwiftTrack - The Operating System for Modern Logistics",
  description: "Unified Delivery Orchestration Platform for Fleets, 3PLs, and Marketplaces.",
  icons: {
    icon: [
      { url: "/favicon.ico" },
      { url: "/icon.svg", type: "image/svg+xml" },
    ],
    shortcut: "/favicon.ico",
    apple: "/icon.svg",
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className={`${inter.className} antialiased min-h-screen bg-background text-foreground selection:bg-primary/30`}>
        <AppShell>{children}</AppShell>
        <NotificationManager />
        <Toaster />
      </body>
    </html>
  );
}
