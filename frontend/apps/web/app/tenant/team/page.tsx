"use client";

import React, { useState } from 'react';
import { 
  Users, Search, UserPlus, MoreVertical, Building2, RefreshCw, Bell, CheckCircle2 
} from 'lucide-react';
import { useAuthStore } from '@/store/useAuthStore';

// Mock data based on the provided design
const MOCK_STAFF = [
  { id: 1, name: 'Michael Chen', email: 'michael.c@swifttrack.com', role: 'OPERATIONS LEAD', status: 'Active', inviteDate: 'Oct 12, 2023', avatar: 'https://api.dicebear.com/7.x/initials/svg?seed=MC&backgroundColor=3e5bf2' },
  { id: 2, name: 'Sarah Jenkins', email: 's.jenkins@swifttrack.com', role: 'DISPATCHER', status: 'Active', inviteDate: 'Nov 05, 2023', avatar: 'https://api.dicebear.com/7.x/initials/svg?seed=SJ&backgroundColor=00dce5' },
  { id: 3, name: 'Alex Rivera', email: 'a.rivera@logistics.net', role: 'FLEET MANAGER', status: 'Pending', inviteDate: 'Jan 18, 2024', avatar: null }
];

export default function TenantTeamPage() {
  const { user } = useAuthStore();
  const [isInviteModalOpen, setIsInviteModalOpen] = useState(false);

  return (
    <div className="p-12 space-y-16 text-[#dae2fd]">
      
      {/* Header Section */}
      <section className="flex justify-between items-end">
        <div>
          <h1 className="font-['Manrope'] text-5xl font-extrabold tracking-tight text-white mb-2">Team & Providers</h1>
          <p className="text-[#c5c5d8] max-w-md">
            Orchestrate your logistics network and internal workforce from a single kinetic interface.
          </p>
        </div>
        <button 
          onClick={() => setIsInviteModalOpen(true)}
          className="px-8 py-4 bg-gradient-to-r from-[#3e5bf2] to-[#00787d] text-white font-bold rounded-full hover:shadow-[0px_0px_20px_rgba(62,91,242,0.4)] transition-all active:scale-95 flex items-center gap-2"
        >
          <UserPlus className="w-5 h-5" />
          Invite New Member
        </button>
      </section>

      {/* Staff Roster Table */}
      <section className="space-y-6">
        <div className="flex items-center justify-between">
          <h2 className="font-['Manrope'] text-2xl font-bold text-white">Staff Roster</h2>
          <div className="flex gap-2">
            <span className="bg-[#2d3449] px-4 py-1.5 rounded-full text-xs font-medium text-[#bac3ff]">All ({MOCK_STAFF.length})</span>
            <span className="bg-[#131b2e] px-4 py-1.5 rounded-full text-xs font-medium text-[#c5c5d8]">Admins</span>
            <span className="bg-[#131b2e] px-4 py-1.5 rounded-full text-xs font-medium text-[#c5c5d8]">Dispatch</span>
          </div>
        </div>

        <div className="bg-[#171f33] rounded-[2rem] overflow-hidden shadow-2xl border border-white/5">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-[#222a3d]/50">
                <th className="px-8 py-5 text-xs font-bold uppercase tracking-wider text-[#c5c5d8]">Staff Name</th>
                <th className="px-8 py-5 text-xs font-bold uppercase tracking-wider text-[#c5c5d8]">Role</th>
                <th className="px-8 py-5 text-xs font-bold uppercase tracking-wider text-[#c5c5d8]">Status</th>
                <th className="px-8 py-5 text-xs font-bold uppercase tracking-wider text-[#c5c5d8]">Invite Date</th>
                <th className="px-8 py-5 text-xs font-bold uppercase tracking-wider text-[#c5c5d8] text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5">
              {MOCK_STAFF.map(staff => (
                <tr key={staff.id} className="hover:bg-[#2d3449]/30 transition-colors group">
                  <td className="px-8 py-6">
                    <div className="flex items-center gap-4">
                      <div className="w-10 h-10 rounded-full bg-[#2d3449] flex items-center justify-center text-[#bac3ff] font-bold overflow-hidden border-2 border-white/5">
                        {staff.avatar ? (
                          <img src={staff.avatar} alt="Staff avatar" className="w-full h-full object-cover" />
                        ) : (
                          <Users className="w-5 h-5 opacity-50" />
                        )}
                      </div>
                      <div>
                        <p className="text-sm font-bold text-white">{staff.name}</p>
                        <p className="text-xs text-[#c5c5d8]">{staff.email}</p>
                      </div>
                    </div>
                  </td>
                  <td className="px-8 py-6">
                    <span className="px-3 py-1 bg-[#3e5bf2]/10 text-[#3e5bf2] text-[10px] font-bold rounded-full border border-[#3e5bf2]/20">
                      {staff.role}
                    </span>
                  </td>
                  <td className="px-8 py-6">
                    <div className="flex items-center gap-2">
                      <div className={`w-2 h-2 rounded-full ${staff.status === 'Active' ? 'bg-[#00dce5] animate-pulse' : 'bg-[#8e8fa1]'}`}></div>
                      <span className={`text-xs font-medium ${staff.status === 'Active' ? 'text-[#00dce5]' : 'text-[#8e8fa1]'}`}>{staff.status}</span>
                    </div>
                  </td>
                  <td className="px-8 py-6 text-xs text-[#c5c5d8]">{staff.inviteDate}</td>
                  <td className="px-8 py-6 text-right">
                    <button className="text-[#c5c5d8] hover:text-white transition-colors">
                      <MoreVertical className="w-5 h-5" />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {/* Courier Integrations */}
      <section className="space-y-6">
        <div className="flex items-center justify-between">
          <h2 className="font-['Manrope'] text-2xl font-bold text-white">Courier Integrations</h2>
          <p className="text-xs text-[#c5c5d8] flex items-center gap-2">
            <RefreshCw className="w-3 h-3" /> Last updated 2 mins ago
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <div className="bg-[#171f33] rounded-2xl p-6 border-b-4 border-yellow-500 hover:-translate-y-1 transition-all duration-300">
             <div className="flex justify-between items-start mb-8">
               <div className="w-12 h-12 bg-white rounded-lg flex items-center justify-center p-2 shadow-inner">
                 <Building2 className="w-8 h-8 text-yellow-500"/>
               </div>
               <div className="relative inline-block w-10 h-6">
                 <input type="checkbox" defaultChecked className="peer appearance-none w-full h-full bg-[#2d3449] rounded-full cursor-pointer checked:bg-[#00dce5] transition-colors" />
                 <span className="absolute left-1 top-1 w-4 h-4 bg-[#c5c5d8] rounded-full peer-checked:translate-x-4 peer-checked:bg-white transition-transform"></span>
               </div>
             </div>
             <h3 className="font-['Manrope'] text-xl font-bold text-white mb-1">DHL Express</h3>
             <p className="text-xs text-[#c5c5d8] mb-6">Global logistics & shipping services.</p>
             <button className="w-full py-2.5 bg-[#2d3449] text-[#bac3ff] text-xs font-bold rounded-lg hover:bg-[#3e5bf2]/10 transition-colors">Configure API</button>
          </div>

          <div className="bg-[#171f33] rounded-2xl p-6 border-b-4 border-purple-600 hover:-translate-y-1 transition-all duration-300">
             <div className="flex justify-between items-start mb-8">
               <div className="w-12 h-12 bg-white rounded-lg flex items-center justify-center p-2 shadow-inner">
                 <Building2 className="w-8 h-8 text-purple-600"/>
               </div>
               <div className="relative inline-block w-10 h-6">
                 <input type="checkbox" defaultChecked className="peer appearance-none w-full h-full bg-[#2d3449] rounded-full cursor-pointer checked:bg-[#00dce5] transition-colors" />
                 <span className="absolute left-1 top-1 w-4 h-4 bg-[#c5c5d8] rounded-full peer-checked:translate-x-4 peer-checked:bg-white transition-transform"></span>
               </div>
             </div>
             <h3 className="font-['Manrope'] text-xl font-bold text-white mb-1">FedEx</h3>
             <p className="text-xs text-[#c5c5d8] mb-6">Overnight courier & freight delivery.</p>
             <button className="w-full py-2.5 bg-[#2d3449] text-[#bac3ff] text-xs font-bold rounded-lg hover:bg-[#3e5bf2]/10 transition-colors">Configure API</button>
          </div>
          
          <div className="bg-[#171f33] rounded-2xl p-6 border-b-4 border-[#00dce5] hover:-translate-y-1 transition-all duration-300">
             <div className="flex justify-between items-start mb-8">
               <div className="w-12 h-12 bg-[#00dce5]/20 rounded-lg flex items-center justify-center p-2">
                 <Building2 className="w-8 h-8 text-[#00dce5]"/>
               </div>
               <div className="relative inline-block w-10 h-6">
                 <input type="checkbox" defaultChecked className="peer appearance-none w-full h-full bg-[#2d3449] rounded-full cursor-pointer checked:bg-[#00dce5] transition-colors" />
                 <span className="absolute left-1 top-1 w-4 h-4 bg-[#c5c5d8] rounded-full peer-checked:translate-x-4 peer-checked:bg-white transition-transform"></span>
               </div>
             </div>
             <h3 className="font-['Manrope'] text-xl font-bold text-white mb-1">Local Express</h3>
             <p className="text-xs text-[#c5c5d8] mb-6">Last-mile delivery integration.</p>
             <button className="w-full py-2.5 bg-[#2d3449] text-[#bac3ff] text-xs font-bold rounded-lg hover:bg-[#3e5bf2]/10 transition-colors">Configure API</button>
          </div>
        </div>
      </section>

      {/* Invite Modal Overlay */}
      {isInviteModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-6 bg-[#0b1326]/80 backdrop-blur-sm">
          <div className="bg-[#131b2e]/90 backdrop-blur-xl w-full max-w-lg rounded-2xl p-10 shadow-2xl border border-white/10 relative">
            <button 
              onClick={() => setIsInviteModalOpen(false)}
              className="absolute top-6 right-6 w-10 h-10 flex items-center justify-center rounded-full bg-[#2d3449]/50 hover:bg-white/10 transition-colors text-white"
            >
              x
            </button>
            
            <h2 className="font-['Manrope'] text-3xl font-extrabold text-white">Invite Member</h2>
            <p className="text-[#c5c5d8] mt-1 mb-8">Grant access to the command center.</p>
            
            <form className="space-y-6">
              <div className="space-y-2">
                <label className="text-xs font-bold uppercase tracking-widest text-[#bac3ff]">Full Name</label>
                <input 
                  type="text"
                  placeholder="e.g. Katherine Pierce"
                  className="w-full bg-[#2d3449]/60 border-none rounded-xl py-4 px-5 text-white focus:ring-2 focus:ring-[#3e5bf2]/50 placeholder:text-[#c5c5d8]/30 outline-none" 
                />
              </div>
              <div className="space-y-2">
                <label className="text-xs font-bold uppercase tracking-widest text-[#bac3ff]">Email Address</label>
                <input 
                  type="email"
                  placeholder="katherine@company.com"
                  className="w-full bg-[#2d3449]/60 border-none rounded-xl py-4 px-5 text-white focus:ring-2 focus:ring-[#3e5bf2]/50 placeholder:text-[#c5c5d8]/30 outline-none" 
                />
              </div>
              <div className="pt-4">
                <button type="button" className="w-full py-4 bg-gradient-to-r from-[#3e5bf2] to-[#00787d] text-white font-black text-lg rounded-full shadow-lg hover:scale-105 active:scale-95 transition-all outline-none">
                  Send Digital Invitation
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
