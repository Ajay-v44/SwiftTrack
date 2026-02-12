"use client"

import { motion } from "framer-motion"

export function LiveMap() {
    return (
        <div className="relative w-full h-64 bg-neutral-950 rounded-xl overflow-hidden border border-white/10 group">
            {/* Map styling */}
            <div className="absolute inset-0 opacity-10">
                <svg width="100%" height="100%">
                    <pattern id="mapGrid" width="40" height="40" patternUnits="userSpaceOnUse">
                        <path d="M 40 0 L 0 0 0 40" fill="none" stroke="currentColor" strokeWidth="0.5" />
                    </pattern>
                    <rect width="100%" height="100%" fill="url(#mapGrid)" />
                </svg>
            </div>

            {/* Route Path */}
            <svg className="absolute inset-0 w-full h-full pointer-events-none">
                <motion.path
                    d="M 50 150 C 100 100, 200 200, 300 120"
                    fill="none"
                    stroke="#38bdf8"
                    strokeWidth="2"
                    strokeDasharray="4 4"
                    initial={{ pathLength: 0 }}
                    animate={{ pathLength: 1 }}
                    transition={{ duration: 3, ease: "easeInOut" }}
                />

                {/* Moving Vehicle */}
                <motion.circle
                    r="4"
                    fill="#38bdf8"
                    filter="url(#glow)"
                >
                    <animateMotion
                        dur="3s"
                        repeatCount="indefinite"
                        path="M 50 150 C 100 100, 200 200, 300 120"
                    />
                </motion.circle>
                <defs>
                    <filter id="glow">
                        <feGaussianBlur stdDeviation="2" result="coloredBlur" />
                        <feMerge>
                            <feMergeNode in="coloredBlur" />
                            <feMergeNode in="SourceGraphic" />
                        </feMerge>
                    </filter>
                </defs>
            </svg>

            {/* Location Cards */}
            <div className="absolute top-4 right-4 bg-neutral-900/90 backdrop-blur border border-white/10 p-2 rounded-lg text-xs font-mono">
                <div className="text-primary mb-1">● ON_ROUTE</div>
                <div className="text-muted-foreground">ETA: 14 MIN</div>
            </div>
        </div>
    )
}
