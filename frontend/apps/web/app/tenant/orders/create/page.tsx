"use client";

import React, { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { 
  ArrowLeft, MapPin, Navigation, Box, Weight, 
  BarChart4, Rocket, CheckCircle2, Loader2, ArrowRight
} from 'lucide-react';
import { useAuthStore } from '@/store/useAuthStore';
import apiClient from '@/lib/api-client';
import { toast } from 'sonner';

export default function CreateOrderPage() {
  const router = useRouter();
  const { user } = useAuthStore();
  const [step, setStep] = useState(1);
  const [isLoading, setIsLoading] = useState(false);
  
  const [formData, setFormData] = useState({
    originAddress: '', originCity: '', originZip: '',
    destAddress: '', destCity: '', destZip: '',
    weight: '', dimensions: '', type: 'Standard Cargo'
  });

  const [quotes, setQuotes] = useState<any[]>([]);
  const [selectedQuote, setSelectedQuote] = useState<string | null>(null);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setFormData(prev => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleGetQuotes = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      // Mocking the get quotes API call based on the backend structure (requires proper body map)
      /* 
      const res = await apiClient.post('/api/order/v1/tenant/quote', { ...formData }, {
        params: { id: user?.id }
      });
      setQuotes(res.data);
      */
      
      // Simulating API latency & response for demo
      setTimeout(() => {
        setQuotes([
          { id: 'Q-1001', provider: 'DHL Express', price: 145.00, eta: '2 Days', tag: 'Fastest' },
          { id: 'Q-1002', provider: 'FedEx Freight', price: 120.00, eta: '3 Days', tag: 'Best Value' },
          { id: 'Q-1003', provider: 'Internal Fleet', price: 95.00, eta: '5 Days', tag: 'Lowest Cost' }
        ]);
        setStep(2);
        setIsLoading(false);
      }, 1500);
      
    } catch (err: any) {
      toast.error('Failed to fetch quotes');
      setIsLoading(false);
    }
  };

  const handleDispatch = async () => {
    if (!selectedQuote) {
      toast.error('Please select a service quote');
      return;
    }
    setIsLoading(true);
    try {
      // API call to bind quote and create order
      /* await apiClient.post('/api/order/v1/tenant/create', { quoteId: selectedQuote }, { params: { id: user?.id }}); */
      setTimeout(() => {
        toast.success('Order dispatched successfully!');
        router.push('/tenant/orders');
      }, 1500);
    } catch (err) {
      toast.error('Failed to dispatch order');
      setIsLoading(false);
    }
  };

  return (
    <div className="p-12 space-y-10 text-[#dae2fd] max-w-6xl mx-auto">
      
      {/* Header */}
      <div className="flex items-center gap-6">
        <button onClick={() => router.back()} className="w-12 h-12 rounded-full bg-[#131b2e] border border-white/5 flex items-center justify-center text-[#c5c5d8] hover:text-white hover:bg-[#2d3449] transition-colors">
          <ArrowLeft className="w-5 h-5" />
        </button>
        <div>
          <h1 className="font-['Manrope'] text-4xl font-extrabold text-white tracking-tight">Create New Shipment</h1>
          <p className="text-[#c5c5d8] text-sm mt-1">Configure parameters and request instant quotes from integrated providers.</p>
        </div>
      </div>

      {/* Stepper */}
      <div className="flex items-center justify-between w-full max-w-2xl mx-auto mb-16 relative">
        <div className="absolute top-5 left-0 w-full h-0.5 bg-[#2d3449] -z-10">
          <div className="h-full bg-gradient-to-r from-[#3e5bf2] to-[#00dce5] transition-all duration-700" style={{ width: step === 1 ? '0%' : '50%' }}></div>
        </div>
        
        <div className="flex flex-col items-center gap-3">
          <div className={`w-10 h-10 rounded-full flex items-center justify-center font-bold text-sm ring-4 ring-[#0b1326] transition-colors ${step >= 1 ? 'bg-[#3e5bf2] text-white shadow-[0_0_15px_rgba(62,91,242,0.4)]' : 'bg-[#2d3449] text-[#c5c5d8]'}`}>
            1
          </div>
          <span className={`text-[10px] font-bold uppercase tracking-widest ${step >= 1 ? 'text-[#bac3ff]' : 'text-[#8e8fa1]'}`}>Setup</span>
        </div>

        <div className="flex flex-col items-center gap-3">
          <div className={`w-10 h-10 rounded-full flex items-center justify-center font-bold text-sm ring-4 ring-[#0b1326] transition-colors ${step >= 2 ? 'bg-[#00dce5] text-[#002021] shadow-[0_0_15px_rgba(0,220,229,0.4)]' : 'bg-[#2d3449] text-[#c5c5d8]'}`}>
            2
          </div>
          <span className={`text-[10px] font-bold uppercase tracking-widest ${step >= 2 ? 'text-[#00dce5]' : 'text-[#8e8fa1]'}`}>Dispatch</span>
        </div>

        <div className="flex flex-col items-center gap-3">
          <div className={`w-10 h-10 rounded-full flex items-center justify-center font-bold text-sm ring-4 ring-[#0b1326] transition-colors ${step >= 3 ? 'bg-emerald-400 text-emerald-950 shadow-[0_0_15px_rgba(52,211,153,0.4)]' : 'bg-[#2d3449] text-[#c5c5d8]'}`}>
            3
          </div>
          <span className={`text-[10px] font-bold uppercase tracking-widest ${step >= 3 ? 'text-emerald-400' : 'text-[#8e8fa1]'}`}>Active</span>
        </div>
      </div>

      <div className="bg-[#131b2e] border border-white/5 shadow-2xl rounded-[2rem] p-10 relative overflow-hidden">
        
        {step === 1 && (
          <form onSubmit={handleGetQuotes} className="space-y-12">
            
            {/* Origin & Destination Grid */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-10 relative">
              
              {/* Animated Origin to Destination Path */}
              <div className="hidden lg:block absolute left-1/2 top-10 bottom-10 w-0.5 bg-gradient-to-b from-[#3e5bf2] via-[#00dce5] to-transparent -translate-x-1/2 opacity-30"></div>

              {/* Origin */}
              <div className="space-y-6">
                <div className="flex items-center gap-3 mb-6">
                  <div className="w-10 h-10 rounded-full bg-[#3e5bf2]/20 flex items-center justify-center text-[#3e5bf2]">
                    <MapPin className="w-5 h-5" />
                  </div>
                  <h3 className="font-['Manrope'] text-xl font-bold text-white">Origin Details</h3>
                </div>

                <div className="space-y-4">
                  <div className="space-y-2">
                    <label className="text-[10px] uppercase font-bold tracking-widest text-[#bac3ff] ml-1">Street Address</label>
                    <input 
                      type="text" required name="originAddress" value={formData.originAddress} onChange={handleChange}
                      placeholder="123 Logistics Way" 
                      className="w-full bg-[#171f33] border border-transparent rounded-xl px-4 py-3 text-sm text-white focus:border-[#3e5bf2] focus:ring-1 focus:ring-[#3e5bf2] outline-none transition-all placeholder:text-[#8e8fa1]" 
                    />
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <label className="text-[10px] uppercase font-bold tracking-widest text-[#bac3ff] ml-1">City</label>
                      <input 
                        type="text" required name="originCity" value={formData.originCity} onChange={handleChange}
                        placeholder="Atlanta" 
                        className="w-full bg-[#171f33] border border-transparent rounded-xl px-4 py-3 text-sm text-white focus:border-[#3e5bf2] focus:ring-1 focus:ring-[#3e5bf2] outline-none transition-all placeholder:text-[#8e8fa1]" 
                      />
                    </div>
                    <div className="space-y-2">
                      <label className="text-[10px] uppercase font-bold tracking-widest text-[#bac3ff] ml-1">ZIP / Postal</label>
                      <input 
                        type="text" required name="originZip" value={formData.originZip} onChange={handleChange}
                        placeholder="30301" 
                        className="w-full bg-[#171f33] border border-transparent rounded-xl px-4 py-3 text-sm text-white focus:border-[#3e5bf2] focus:ring-1 focus:ring-[#3e5bf2] outline-none transition-all placeholder:text-[#8e8fa1]" 
                      />
                    </div>
                  </div>
                </div>
              </div>

              {/* Destination */}
              <div className="space-y-6">
                <div className="flex items-center gap-3 mb-6">
                  <div className="w-10 h-10 rounded-full bg-[#00dce5]/20 flex items-center justify-center text-[#00dce5]">
                    <Navigation className="w-5 h-5" />
                  </div>
                  <h3 className="font-['Manrope'] text-xl font-bold text-white">Destination Details</h3>
                </div>

                <div className="space-y-4">
                  <div className="space-y-2">
                    <label className="text-[10px] uppercase font-bold tracking-widest text-[#00dce5] ml-1">Street Address</label>
                    <input 
                      type="text" required name="destAddress" value={formData.destAddress} onChange={handleChange}
                      placeholder="456 Destination Blvd" 
                      className="w-full bg-[#171f33] border border-transparent rounded-xl px-4 py-3 text-sm text-white focus:border-[#00dce5] focus:ring-1 focus:ring-[#00dce5] outline-none transition-all placeholder:text-[#8e8fa1]" 
                    />
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <label className="text-[10px] uppercase font-bold tracking-widest text-[#00dce5] ml-1">City</label>
                      <input 
                        type="text" required name="destCity" value={formData.destCity} onChange={handleChange}
                        placeholder="Seattle" 
                        className="w-full bg-[#171f33] border border-transparent rounded-xl px-4 py-3 text-sm text-white focus:border-[#00dce5] focus:ring-1 focus:ring-[#00dce5] outline-none transition-all placeholder:text-[#8e8fa1]" 
                      />
                    </div>
                    <div className="space-y-2">
                      <label className="text-[10px] uppercase font-bold tracking-widest text-[#00dce5] ml-1">ZIP / Postal</label>
                      <input 
                        type="text" required name="destZip" value={formData.destZip} onChange={handleChange}
                        placeholder="98101" 
                        className="w-full bg-[#171f33] border border-transparent rounded-xl px-4 py-3 text-sm text-white focus:border-[#00dce5] focus:ring-1 focus:ring-[#00dce5] outline-none transition-all placeholder:text-[#8e8fa1]" 
                      />
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <hr className="border-[#2d3449]/50" />

            {/* Shipment Specs */}
            <div className="space-y-6">
              <div className="flex items-center gap-3 mb-6">
                <div className="w-10 h-10 rounded-full bg-[#8e8fa1]/20 flex items-center justify-center text-[#c5c5d8]">
                  <Box className="w-5 h-5" />
                </div>
                <h3 className="font-['Manrope'] text-xl font-bold text-white">Shipment Specifications</h3>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div className="space-y-2 border border-white/5 bg-[#171f33] rounded-2xl px-5 py-4 focus-within:border-[#3e5bf2]/50 transition-colors">
                  <label className="text-[10px] uppercase font-bold tracking-widest text-[#c5c5d8] flex items-center gap-2"><Weight className="w-3 h-3"/> Total Weight (kg)</label>
                  <input type="number" required name="weight" value={formData.weight} onChange={handleChange} className="w-full bg-transparent border-none text-xl font-bold text-white outline-none placeholder:text-[#444655]" placeholder="0.00" />
                </div>
                <div className="space-y-2 border border-white/5 bg-[#171f33] rounded-2xl px-5 py-4 focus-within:border-[#3e5bf2]/50 transition-colors">
                  <label className="text-[10px] uppercase font-bold tracking-widest text-[#c5c5d8] flex items-center gap-2"><BarChart4 className="w-3 h-3"/> Dimensions (L×W×H cm)</label>
                  <input type="text" required name="dimensions" value={formData.dimensions} onChange={handleChange} className="w-full bg-transparent border-none text-xl font-bold text-white outline-none placeholder:text-[#444655]" placeholder="10×10×10" />
                </div>
                <div className="space-y-2 border border-white/5 bg-[#171f33] rounded-2xl px-5 py-4 focus-within:border-[#3e5bf2]/50 transition-colors">
                  <label className="text-[10px] uppercase font-bold tracking-widest text-[#c5c5d8] flex items-center gap-2"><Rocket className="w-3 h-3"/> Freight Type</label>
                  <select name="type" value={formData.type} onChange={handleChange} className="w-full bg-transparent border-none text-sm font-bold text-[#bac3ff] outline-none cursor-pointer appearance-none mt-1">
                    <option className="bg-[#171f33]" value="Standard Cargo">Standard Cargo</option>
                    <option className="bg-[#171f33]" value="Fragile Express">Fragile / Express</option>
                    <option className="bg-[#171f33]" value="Palletized Freight">Palletized Freight</option>
                  </select>
                </div>
              </div>
            </div>

            <div className="pt-4 flex justify-end">
              <button 
                type="submit" 
                disabled={isLoading}
                className="bg-gradient-to-r from-[#3e5bf2] to-[#00dce5] text-white px-8 py-4 rounded-full font-bold text-sm shadow-[0_0_20px_rgba(62,91,242,0.3)] hover:shadow-[0_0_30px_rgba(62,91,242,0.5)] transition-all active:scale-[0.98] disabled:opacity-70 flex items-center gap-2"
              >
                {isLoading ? <Loader2 className="w-5 h-5 animate-spin"/> : 'Request Quotes & Proceed'}
                {!isLoading && <ArrowRight className="w-5 h-5" />}
              </button>
            </div>
          </form>
        )}

        {step === 2 && (
          <div className="space-y-8 animate-in fade-in zoom-in-95 duration-500">
            <div className="text-center max-w-xl mx-auto mb-12">
              <h3 className="font-['Manrope'] text-3xl font-extrabold text-white mb-2">Select a Service Quote</h3>
              <p className="text-[#c5c5d8]">Based on your specifications, we've sourced the best dynamic rates from your active integrations.</p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              {quotes.map((quote) => (
                <div 
                  key={quote.id} 
                  onClick={() => setSelectedQuote(quote.id)}
                  className={`relative p-8 rounded-[2rem] border-2 cursor-pointer transition-all ${selectedQuote === quote.id ? 'bg-[#3e5bf2]/10 border-[#3e5bf2] shadow-[0_0_30px_rgba(62,91,242,0.2)] scale-105' : 'bg-[#171f33] border-transparent hover:border-white/10 hover:bg-[#2d3449]/50'}`}
                >
                  {quote.tag && (
                    <span className={`absolute -top-3 left-1/2 -translate-x-1/2 px-4 py-1 rounded-full text-[10px] font-black uppercase tracking-widest ${quote.tag === 'Fastest' ? 'bg-[#00dce5] text-[#002021]' : 'bg-[#2d3449] text-white'}`}>
                      {quote.tag}
                    </span>
                  )}
                  {selectedQuote === quote.id && <div className="absolute top-4 right-4 w-6 h-6 rounded-full bg-[#3e5bf2] text-white flex items-center justify-center"><CheckCircle2 className="w-4 h-4"/></div>}
                  
                  <div className="text-center mb-6 mt-4">
                     <p className="text-[#c5c5d8] text-sm font-bold mb-2">{quote.provider}</p>
                     <h4 className="font-['Manrope'] text-4xl font-extrabold text-white">${quote.price.toFixed(2)}</h4>
                  </div>
                  
                  <div className="bg-[#0b1326] rounded-xl p-4 flex items-center justify-between text-sm">
                    <span className="text-[#8e8fa1]">Est. Delivery</span>
                    <span className="font-bold text-[#00dce5]">{quote.eta}</span>
                  </div>
                </div>
              ))}
            </div>

            <div className="pt-10 flex items-center justify-between border-t border-[#2d3449]/50">
              <button 
                onClick={() => setStep(1)}
                className="text-[#c5c5d8] font-bold text-sm hover:text-white transition-colors"
              >
                ← Back to Edit Specs
              </button>
              <button 
                onClick={handleDispatch}
                disabled={isLoading || !selectedQuote}
                className="bg-emerald-500 hover:bg-emerald-400 text-white px-10 py-4 rounded-full font-black text-sm shadow-[0_0_20px_rgba(16,185,129,0.3)] transition-all active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
              >
                {isLoading ? <Loader2 className="w-5 h-5 animate-spin" /> : 'Confirm & Dispatch Fleet'}
                {!isLoading && <Rocket className="w-5 h-5 ml-1" />}
              </button>
            </div>
          </div>
        )}

      </div>
    </div>
  );
}
