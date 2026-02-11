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
        <section className="py-24 border-y border-white/5 bg-primary/5 relative overflow-hidden">
            <div className="absolute inset-0 bg-[url('/grid.svg')] opacity-10" />
            <div className="container mx-auto px-4 relative z-10">
                <div className="grid grid-cols-2 md:grid-cols-4 gap-8 text-center">
                    {stats.map((stat, i) => (
                        <motion.div
                            key={i}
                            initial={{ opacity: 0, scale: 0.5 }}
                            whileInView={{ opacity: 1, scale: 1 }}
                            transition={{ delay: i * 0.1, duration: 0.5, type: "spring" }}
                            viewport={{ once: true }}
                        >
                            <div className="text-4xl md:text-5xl font-bold bg-clip-text text-transparent bg-gradient-to-b from-white to-white/50 mb-2">
                                {stat.value}
                            </div>
                            <div className="text-sm md:text-base text-muted-foreground font-medium uppercase tracking-wider">
                                {stat.label}
                            </div>
                        </motion.div>
                    ))}
                </div>
            </div>
        </section>
    )
}
