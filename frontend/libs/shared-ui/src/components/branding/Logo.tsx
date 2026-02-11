import Link from "next/link"
import { motion } from "framer-motion"

export function Logo({ className = "" }: { className?: string }) {
    return (
        <Link href="/" className={`flex items-center gap-2 group ${className}`}>
            <div className="relative w-8 h-8 md:w-10 md:h-10">
                <div className="absolute inset-0 bg-primary/20 rounded-xl blur-lg group-hover:bg-primary/40 transition-colors" />
                <div className="relative w-full h-full bg-gradient-to-br from-primary to-purple-600 rounded-xl flex items-center justify-center text-white shadow-lg overflow-hidden border border-white/20">
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        strokeWidth="2.5"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        className="w-5 h-5 md:w-6 md:h-6 transform -rotate-45 group-hover:rotate-0 transition-transform duration-300"
                    >
                        <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" />
                    </svg>
                </div>
            </div>
            <div className="flex flex-col">
                <span className="font-bold text-lg md:text-xl tracking-tight leading-none">SwiftTrack</span>
                <span className="text-[10px] md:text-xs text-muted-foreground uppercase tracking-widest leading-none">Logistics</span>
            </div>
        </Link>
    )
}
