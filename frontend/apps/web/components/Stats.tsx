"use client"

import { motion } from "framer-motion"

const stats = [
    { label: "Orders Delivered", value: "10M+" },
    { label: "Active Drivers", value: "50k+" },
    { label: "Cities Covered", value: "250+" },
    { label: "Uptime", value: "99.99%" },
]

export function Stats() {
    return (
        <section className="py-24 border-y border-slate-200 bg-gradient-to-br from-slate-950 via-slate-900 to-blue-950 relative overflow-hidden">
            <div className="absolute inset-0 bg-[radial-gradient(circle_at_top,rgba(56,189,248,0.18),transparent_35%)]" />
            <div className="container mx-auto px-4 relative z-10">
                <div className="grid grid-cols-2 md:grid-cols-4 gap-8 text-center">
                    {stats.map((stat, i) => (
                        <motion.div
                            key={i}
                            initial={{ opacity: 0, scale: 0.5 }}
                            whileInView={{ opacity: 1, scale: 1 }}
                            transition={{ delay: i * 0.1, duration: 0.5, type: "spring" }}
                            viewport={{ once: true }}
                            className="rounded-3xl border border-white/15 bg-white/10 px-6 py-8 shadow-[0_30px_80px_-50px_rgba(59,130,246,0.9)] backdrop-blur"
                        >
                            <div className="text-4xl md:text-5xl font-bold text-white mb-2">
                                {stat.value}
                            </div>
                            <div className="text-sm md:text-base text-slate-200 font-semibold uppercase tracking-[0.2em]">
                                {stat.label}
                            </div>
                        </motion.div>
                    ))}
                </div>
            </div>
        </section>
    )
}
