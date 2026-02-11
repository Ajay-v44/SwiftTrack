import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { Navbar } from "@/components/Navbar";
import { Footer } from "@/components/Footer";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "SwiftTrack - The Operating System for Modern Logistics",
  description: "Unified Delivery Orchestration Platform for Fleets, 3PLs, and Marketplaces.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className={`${inter.className} antialiased min-h-screen bg-background text-foreground selection:bg-primary/30`}>
        <Navbar />
        {children}
        <Footer />
      </body>
    </html>
  );
}
