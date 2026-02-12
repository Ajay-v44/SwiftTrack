import Link from "next/link"
import { motion } from "framer-motion"
import { SwiftTrackLogo } from "@swifttrack/shared-assets"

export function Logo({ className = "" }: { className?: string }) {
    return (
        <Link href="/" className={`flex items-center gap-2 group ${className}`}>
            <div className="relative w-8 h-8 md:w-10 md:h-10">
                <div className="absolute inset-0 bg-primary/20 rounded-xl blur-lg group-hover:bg-primary/40 transition-colors" />
                <div className="relative w-full h-full bg-gradient-to-br from-primary to-purple-600 rounded-xl flex items-center justify-center text-white shadow-lg overflow-hidden border border-white/20">
                    <img
                        src={SwiftTrackLogo}
                        alt="SwiftTrack Logo"
                        className="w-full h-full object-cover"
                    />
                </div>
            </div>
            <div className="flex flex-col">
                <span className="font-bold text-lg md:text-xl tracking-tight leading-none">SwiftTrack</span>
                <span className="text-[10px] md:text-xs text-muted-foreground uppercase tracking-widest leading-none">Logistics</span>
            </div>
        </Link>
    )
}
