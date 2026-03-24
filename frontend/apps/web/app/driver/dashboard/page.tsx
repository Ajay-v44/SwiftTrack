"use client";

import React, { useState } from 'react';
import { 
  MapPin, Navigation, PhoneCall, MessageCircle, 
  Map, ArrowRight, ShieldAlert, CheckCircle2, ChevronRight 
} from 'lucide-react';

export default function DriverDashboard() {
  const [slideStatus, setSlideStatus] = useState(0);

  const handleSlide = (e: React.TouchEvent<HTMLDivElement> | React.MouseEvent<HTMLDivElement>) => {
    // simplified mock slider
    setSlideStatus(Math.min(100, slideStatus + 5));
    if (slideStatus > 90) setSlideStatus(100);
  };

  return (
    <div className="flex flex-col h-full bg-[#020617] text-slate-300">
      
      {/* Mock Map Area */}
      <div className="relative h-[45vh] w-full bg-[#1e293b] flex-shrink-0 shadow-[0_10px_50px_rgba(0,0,0,0.5)] overflow-hidden">
        <div className="absolute inset-0 bg-[url('https://images.unsplash.com/photo-1524661135-423995f22d0b?q=80&w=2074&auto=format&fit=crop')] bg-cover bg-center mix-blend-screen opacity-50 grayscale contrast-150"></div>
        <div className="absolute inset-0 bg-gradient-to-t from-[#020617] via-transparent to-[#020617]/50 pointer-events-none"></div>
        
        {/* Route Path SVG Mock */}
         <svg className="absolute inset-0 w-full h-full stroke-indigo-500 drop-shadow-[0_0_10px_rgba(99,102,241,0.8)] z-10" fill="none" strokeWidth="6" strokeLinecap="round" strokeDasharray="10 15">
           <path d="M 50 150 Q 150 50 250 200 T 350 150" className="animate-[dash_2s_linear_infinite]" />
         </svg>

         <div className="absolute bottom-6 left-6 right-6 flex items-center justify-between z-20">
            <div className="bg-indigo-600/90 backdrop-blur-xl px-6 py-4 rounded-3xl shadow-2xl border border-indigo-400/30 flex items-center gap-4">
              <div className="w-12 h-12 rounded-full bg-white flex items-center justify-center text-indigo-900 shadow-inner">
                <Navigation className="w-6 h-6 fill-indigo-900" />
              </div>
              <div>
                <p className="text-3xl font-black text-white font-['Manrope'] drop-shadow-md">14 min</p>
                <p className="text-xs font-bold text-indigo-200 uppercase tracking-widest">3.2 Miles</p>
              </div>
            </div>
            
            <button className="w-16 h-16 rounded-full bg-slate-900/80 backdrop-blur-md border border-slate-700 flex items-center justify-center shadow-xl active:scale-90 transition-transform">
              <Map className="w-7 h-7 text-indigo-400" />
            </button>
         </div>
      </div>

      {/* Current Task Details */}
      <div className="flex-1 -mt-8 relative z-30 bg-[#020617] rounded-t-[3rem] px-6 pt-10 pb-6 flex flex-col justify-between shadow-[0_-20px_40px_rgba(0,0,0,0.5)]">
        
        <div className="absolute top-4 left-1/2 -translate-x-1/2 w-16 h-1.5 bg-slate-800 rounded-full"></div>

        <div>
           <div className="flex justify-between items-start mb-6">
             <div>
               <p className="text-[10px] font-black uppercase tracking-widest text-emerald-400 bg-emerald-400/10 px-3 py-1 rounded-md mb-3 inline-block">Drop-off</p>
               <h2 className="font-['Manrope'] text-3xl font-extrabold text-white leading-none">742 Evergreen Terr.</h2>
               <p className="text-sm text-slate-400 mt-2 font-medium">Springfield, US 01234</p>
             </div>
             
             <button className="w-12 h-12 bg-indigo-500/10 border border-indigo-500/20 rounded-full flex items-center justify-center text-indigo-400 shadow-lg active:scale-95 transition-transform shrink-0">
                <ShieldAlert className="w-5 h-5" />
             </button>
           </div>
           
           <div className="h-[1px] w-full bg-gradient-to-r from-transparent via-slate-800 to-transparent my-6"></div>
           
           <div className="flex gap-4">
             <div className="flex-1 bg-slate-900 p-5 rounded-3xl border border-slate-800/50 flex flex-col shadow-inner">
               <span className="text-[10px] uppercase font-bold text-slate-500 tracking-wider">Receiver</span>
               <span className="text-lg font-bold text-white mt-1 mb-4 font-['Manrope']">Sarah Conors</span>
               <div className="flex gap-2 mt-auto">
                 <button className="flex-1 py-3 bg-indigo-600 rounded-2xl flex items-center justify-center text-white hover:bg-indigo-500 active:scale-95 transition-all shadow-lg"><PhoneCall className="w-5 h-5"/></button>
                 <button className="flex-1 py-3 bg-slate-800 rounded-2xl flex items-center justify-center text-slate-300 hover:bg-slate-700 active:scale-95 transition-all shadow-inner"><MessageCircle className="w-5 h-5"/></button>
               </div>
             </div>
             
             <div className="flex-1 bg-slate-900 p-5 rounded-3xl border border-slate-800/50 flex flex-col justify-between shadow-inner">
               <div>
                  <span className="text-[10px] uppercase font-bold text-slate-500 tracking-wider mb-2 block">Payload</span>
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-amber-500/10 rounded-xl flex items-center justify-center text-amber-500">
                      <span className="font-extrabold text-lg">2</span>
                    </div>
                    <div>
                      <p className="text-sm font-bold text-white">Large Boxes</p>
                      <p className="text-[10px] text-slate-400">45 lbs total</p>
                    </div>
                  </div>
               </div>
               <button className="w-full py-2.5 mt-4 bg-slate-800 text-xs font-bold text-indigo-300 rounded-xl hover:bg-slate-700 transition-colors flex items-center justify-center border border-indigo-500/20">
                 View Details <ChevronRight className="w-4 h-4 ml-1" />
               </button>
             </div>
           </div>
        </div>

        {/* Swipe to Complete Mock */}
        <div className="mt-8 bg-slate-900 rounded-full h-20 flex relative overflow-hidden p-2 shadow-2xl border border-slate-800 items-center justify-center"
             onClick={handleSlide} onTouchMove={handleSlide}>
          {slideStatus === 100 ? (
             <div className="w-full h-full bg-emerald-500 rounded-full flex items-center justify-center animate-in fade-in zoom-in duration-300 shadow-[0_0_30px_rgba(16,185,129,0.4)]">
               <CheckCircle2 className="w-8 h-8 text-white" />
               <span className="font-black text-white ml-3 font-['Manrope'] text-lg">Delivered</span>
             </div>
          ) : (
             <>
                <div className="absolute left-0 top-0 bottom-0 bg-gradient-to-r from-emerald-500/20 to-emerald-400/80 rounded-full transition-all duration-100 ease-out" style={{ width: `${Math.max(20, slideStatus)}%` }}></div>
                <div className="w-16 h-16 bg-emerald-400 rounded-full absolute left-2 flex items-center justify-center shadow-lg transition-all duration-100 ease-out z-10" style={{ transform: `translateX(calc(${slideStatus}% * 3.5))` }}>
                  <ArrowRight className="w-8 h-8 text-emerald-950" />
                </div>
                <span className="font-black text-slate-400/80 uppercase tracking-widest text-sm z-0 pl-16">Slide to Mark Delivered</span>
             </>
          )}
        </div>
        
      </div>

    </div>
  );
}
