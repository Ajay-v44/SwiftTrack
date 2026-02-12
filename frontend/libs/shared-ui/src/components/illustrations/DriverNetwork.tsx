"use client"

import { motion } from "framer-motion"
import { SwiftTrackDriverFleet } from "@swifttrack/shared-assets"

export function DriverNetwork() {
    return (
        <div className="relative w-full h-64 bg-neutral-900/50 rounded-xl overflow-hidden border border-white/10">
            {/* Grid */}
            <div className="absolute inset-0 opacity-20"
                style={{ backgroundImage: 'radial-gradient(#4f4f4f 1px, transparent 1px)', backgroundSize: '20px 20px' }} />

            {/* Image */}
            <div className="absolute inset-0 z-10">
                <img
                    src={SwiftTrackDriverFleet}
                    alt="Driver Fleet"
                    className="w-full h-full object-cover opacity-80"
                />
            </div>

            <div className="absolute bottom-2 left-2 text-[10px] text-muted-foreground font-mono">
                LIVE_NET_STATUS: ACTIVE<br />
                DRIVERS_NEARBY: 142
            </div>
        </div>
    )
}
