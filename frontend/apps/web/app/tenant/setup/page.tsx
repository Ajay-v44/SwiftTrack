"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/store/useAuthStore";
import { useTenantCompanySetup } from "@/hooks/useTenantCompanySetup";
import { toast } from "sonner";
import { Building2, FileCheck, CheckCircle2, ShieldCheck, Loader2, Info } from "lucide-react";
import { Button } from "@/components/ui/button";
import { CompanyRegistrationInput } from "@swifttrack/types";

export default function CompanySetupPage() {
  const router = useRouter();
  const { user } = useAuthStore();
  const { isLoading, submit } = useTenantCompanySetup();

  const [formData, setFormData] = useState<CompanyRegistrationInput>({
    legalName: "",
    registrationNumber: "",
    incorporationDate: "",
    industryVertical: "Third-Party Logistics (3PL)",
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user?.id) {
      toast.error("User ID not found. Please log in again.");
      return;
    }

    try {
      await submit(user.id, formData);
      toast.success("Organization details saved successfully!");
      // Proceed to the command center
      router.push("/tenant/dashboard");
    } catch (error: unknown) {
      console.error(error);
      toast.error("Failed to register company");
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setFormData((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  return (
    <div className="min-h-screen bg-[#0b1326] text-[#dae2fd] font-['Inter'] flex flex-col items-center py-20 px-6">
      
      {/* Header Section */}
      <div className="text-center mb-16 max-w-2xl">
        <h1 className="text-xl font-black mb-6 bg-gradient-to-r from-[#3e5bf2] to-[#00dce5] bg-clip-text text-transparent inline-block tracking-tight">
          SwiftTrack
        </h1>
        <h2 className="font-['Manrope'] text-5xl font-extrabold text-white mb-4 tracking-tight">
          Company Registration
        </h2>
        <p className="text-[#c5c5d8] text-lg">
          Set up your command center and begin orchestrating your logistics network.
        </p>
      </div>

      {/* Stepper Indicator */}
      <div className="flex items-center justify-between w-full max-w-3xl mb-16 relative">
        <div className="absolute top-6 left-0 w-full h-0.5 bg-[#2d3449] -z-10">
          <div className="h-full bg-gradient-to-r from-[#3e5bf2] to-[#00dce5] w-1/4"></div>
        </div>

        <div className="flex flex-col items-center gap-4 cursor-default">
          <div className="w-12 h-12 rounded-full bg-[#3e5bf2] text-white flex items-center justify-center font-bold ring-4 ring-[#3e5bf2]/20 outline outline-8 outline-[#0b1326]">
            <Building2 className="w-5 h-5" />
          </div>
          <span className="text-xs font-bold text-[#bac3ff] uppercase tracking-widest tracking-[0.2em] mt-1 space-y-1">Basic Info</span>
        </div>

        <div className="flex flex-col items-center gap-4 opacity-50 cursor-not-allowed">
          <div className="w-12 h-12 rounded-full bg-[#2d3449] text-[#c5c5d8] flex items-center justify-center font-bold outline outline-8 outline-[#0b1326]">
            <FileCheck className="w-5 h-5" />
          </div>
          <span className="text-xs font-bold text-[#c5c5d8] uppercase tracking-[0.2em] mt-1">Contact</span>
        </div>

        <div className="flex flex-col items-center gap-4 opacity-50 cursor-not-allowed">
          <div className="w-12 h-12 rounded-full bg-[#2d3449] text-[#c5c5d8] flex items-center justify-center font-bold outline outline-8 outline-[#0b1326]">
            <ShieldCheck className="w-5 h-5" />
          </div>
          <span className="text-xs font-bold text-[#c5c5d8] uppercase tracking-[0.2em] mt-1">Verification</span>
        </div>

        <div className="flex flex-col items-center gap-4 opacity-50 cursor-not-allowed">
          <div className="w-12 h-12 rounded-full bg-[#2d3449] text-[#c5c5d8] flex items-center justify-center font-bold outline outline-8 outline-[#0b1326]">
            <CheckCircle2 className="w-5 h-5" />
          </div>
          <span className="text-xs font-bold text-[#c5c5d8] uppercase tracking-[0.2em] mt-1">Complete</span>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-5 gap-8 w-full max-w-5xl">
        
        {/* Main Form Box */}
        <div className="lg:col-span-3 bg-[#131b2e] border border-white/5 rounded-[2rem] p-10 shadow-2xl relative overflow-hidden">
          <div className="absolute top-0 right-0 p-8 pt-10 text-[6rem] font-black text-white/5 leading-none select-none pointer-events-none font-['Manrope']">01</div>
          
          <h3 className="font-['Manrope'] text-2xl font-bold text-white mb-10 pb-6 border-b border-[#2d3449]/50 relative">
            Establish Identity
          </h3>

          <form onSubmit={handleSubmit} className="space-y-8">
            <div className="space-y-3">
              <label className="text-[10px] uppercase font-bold tracking-widest text-[#bac3ff]">Legal Company Name</label>
              <input
                type="text"
                name="legalName"
                required
                value={formData.legalName}
                onChange={handleChange}
                placeholder="e.g. Global Logistics Corp"
                className="w-full bg-[#171f33] border-none rounded-xl px-5 py-4 text-[#dae2fd] text-sm focus:ring-2 focus:ring-[#3e5bf2]/50 placeholder:text-[#c5c5d8]/50 transition-all font-medium"
              />
            </div>

            <div className="grid grid-cols-2 gap-6">
              <div className="space-y-3">
                <label className="text-[10px] uppercase font-bold tracking-widest text-[#bac3ff]">Registration Number</label>
                <input
                  type="text"
                  name="registrationNumber"
                  required
                  value={formData.registrationNumber}
                  onChange={handleChange}
                  placeholder="REG-2024-XXXX"
                  className="w-full bg-[#171f33] border-none rounded-xl px-5 py-4 text-[#dae2fd] text-sm focus:ring-2 focus:ring-[#3e5bf2]/50 placeholder:text-[#c5c5d8]/50 transition-all font-medium"
                />
              </div>

              <div className="space-y-3">
                <label className="text-[10px] uppercase font-bold tracking-widest text-[#bac3ff]">Incorporation Date</label>
                <div className="relative">
                  <input
                    type="date"
                    name="incorporationDate"
                    required
                    value={formData.incorporationDate}
                    onChange={handleChange}
                    className="w-full bg-[#171f33] border-none rounded-xl px-5 py-4 text-[#dae2fd] text-sm focus:ring-2 focus:ring-[#3e5bf2]/50 placeholder:text-[#c5c5d8]/50 transition-all font-medium [color-scheme:dark]"
                  />
                </div>
              </div>
            </div>

            <div className="space-y-3">
              <label className="text-[10px] uppercase font-bold tracking-widest text-[#bac3ff]">Industry Vertical</label>
              <select
                name="industryVertical"
                className="w-full bg-[#171f33] border-none rounded-xl px-5 py-4 text-[#dae2fd] text-sm focus:ring-2 focus:ring-[#3e5bf2]/50 appearance-none bg-[url('data:image/svg+xml;charset=UTF-8,%3csvg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 24 24%22 fill=%22none%22 stroke=%22%23c5c5d8%22 stroke-width=%222%22 stroke-linecap=%22round%22 stroke-linejoin=%22round%22%3e%3cpolyline points=%226 9 12 15 18 9%22%3e%3c/polyline%3e%3c/svg%3e')] bg-[length:20px_20px] bg-[position:right_1.25rem_center] bg-no-repeat font-medium"
                required
                value={formData.industryVertical}
                onChange={handleChange}
              >
                <option value="Third-Party Logistics (3PL)">Third-Party Logistics (3PL)</option>
                <option value="Freight Forwarding">Freight Forwarding</option>
                <option value="E-commerce Fulfillment">E-commerce Fulfillment</option>
                <option value="Direct Carrier">Direct Carrier</option>
                <option value="Enterprise Fleet">Enterprise Fleet</option>
              </select>
            </div>

            <div className="pt-8 flex items-center justify-between border-t border-[#2d3449]/50">
              <button
                type="button"
                className="text-sm font-bold text-[#c5c5d8] hover:text-white transition-colors cursor-pointer"
                onClick={() => router.back()}
              >
                Save Draft
              </button>
              <Button
                type="submit"
                disabled={isLoading}
                className="bg-[#3e5bf2] hover:bg-[#2d4ce4] text-white px-8 py-6 rounded-full font-bold text-sm h-auto transition-all active:scale-[0.98] disabled:opacity-70 flex items-center gap-2"
              >
                {isLoading ? (
                  <Loader2 className="w-4 h-4 animate-spin" />
                ) : (
                  <>
                    Continue
                    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M5 12h14m-7-7l7 7-7 7" />
                    </svg>
                  </>
                )}
              </Button>
            </div>
          </form>
        </div>

        {/* Side Info Panel */}
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-[#171f33] border border-white/5 rounded-[2rem] p-10 shadow-lg h-full flex flex-col justify-center">
            <div className="w-12 h-12 rounded-xl bg-[#2d4ce4]/20 text-[#3e5bf2] flex items-center justify-center mb-8">
              <Info className="w-6 h-6" />
            </div>
            
            <h4 className="font-['Manrope'] text-xl font-bold text-white mb-4">Why we need this?</h4>
            <p className="text-[#c5c5d8] text-sm leading-relaxed mb-8">
              Providing accurate registration data ensures your account is verified swiftly, granting you immediate access to our global carrier network and financial tools.
            </p>

            <ul className="space-y-4">
              <li className="flex items-start gap-3">
                <div className="w-4 h-4 mt-0.5 rounded-full bg-[#00dce5]/20 flex items-center justify-center shrink-0">
                  <div className="w-2 h-2 rounded-full bg-[#00dce5]"></div>
                </div>
                <span className="text-[#c5c5d8] text-xs leading-relaxed">SEC compliant data encryption</span>
              </li>
              <li className="flex items-start gap-3">
                <div className="w-4 h-4 mt-0.5 rounded-full bg-[#00dce5]/20 flex items-center justify-center shrink-0">
                  <div className="w-2 h-2 rounded-full bg-[#00dce5]"></div>
                </div>
                <span className="text-[#c5c5d8] text-xs leading-relaxed">Automated tax ID validation</span>
              </li>
            </ul>
          </div>

          <div className="bg-gradient-to-br from-[#131b2e] to-[#2d3449] p-6 border border-white/5 rounded-2xl relative overflow-hidden group hover:border-[#3e5bf2]/30 transition-colors">
            <img 
              src="https://images.unsplash.com/photo-1586528116311-ad8ed7c83a56?q=80&w=2070&auto=format&fit=crop" 
              alt="Warehouse blur" 
              className="absolute inset-0 w-full h-full object-cover mix-blend-overlay opacity-20 group-hover:scale-105 transition-transform duration-700"
            />
            <div className="relative z-10">
              <p className="text-white italic font-serif leading-relaxed text-sm mb-4">
                &ldquo;SwiftTrack transformed our fleet visibility in under 24 hours.&rdquo;
              </p>
              <div className="flex items-center gap-2">
                <div className="w-6 h-6 rounded-full bg-[#3e5bf2] flex items-center justify-center text-[10px] font-bold">GB</div>
                <span className="text-[10px] font-bold text-[#00dce5] uppercase tracking-widest">Global Cargo Partners</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div className="mt-20 flex justify-center w-full max-w-5xl items-center border-t border-white/5 pt-8 pb-8 text-[#8e8fa1] text-[10px] uppercase font-bold tracking-[0.2em]">
        <span>POWERED BY SWIFTTRACK KINETIC ENGINE © {new Date().getFullYear()}</span>
      </div>
    </div>
  );
}
