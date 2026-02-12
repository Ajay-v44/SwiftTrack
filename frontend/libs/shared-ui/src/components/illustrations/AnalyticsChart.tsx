"use client"

import { motion } from "framer-motion"

export function AnalyticsChart() {
    const bars = [40, 70, 50, 90, 65, 85, 45, 95]

    return (
        <div className="relative w-full h-64 bg-gradient-to-br from-neutral-900 to-neutral-950 rounded-xl overflow-hidden border border-white/10 p-6 flex items-end justify-between gap-2">
            {bars.map((height, i) => (
                <motion.div
                    key={i}
                    className="w-full bg-gradient-to-t from-primary/20 to-primary rounded-t-sm relative group"
                    initial={{ height: 0 }}
                    whileInView={{ height: `${height}%` }}
                    transition={{ duration: 0.8, delay: i * 0.1, ease: "easeOut" }}
                >
                    <div className="absolute -top-8 left-1/2 -translate-x-1/2 opacity-0 group-hover:opacity-100 transition-opacity bg-white text-black text-[10px] font-bold px-1.5 py-0.5 rounded">
                        {height}%
                    </div>
                </motion.div>
            ))}

            {/* Overlay Trend Line */}
            <svg className="absolute inset-0 w-full h-full pointer-events-none opacity-50">
                <motion.path
                    d="M 20 200 L 60 150 L 100 180 L 140 100 L 180 140 L 220 110 L 260 190 L 300 90"
                    fill="none"
                    stroke="#a855f7"
                    strokeWidth="2"
                    initial={{ pathLength: 0 }}
                    whileInView={{ pathLength: 1 }}
                    transition={{ duration: 1.5, delay: 0.5 }}
                />
            </svg>

            <div className="absolute top-4 left-4">
                <div className="text-xs text-muted-foreground uppercase tracking-wider">Performance</div>
                <div className="text-2xl font-bold text-white">+127.5%</div>
            </div>
        </div>
    )
}
