"use client"

import { motion } from "framer-motion"

const companies = [
    "Global Logistics",
    "Swift Courier",
    "Urban Delivery",
    "FastTrack",
    "EcoMove",
    "Prime Cargo",
]

export function TrustedBy() {
    return (
        <section className="py-12 border-y border-white/5 bg-black/20 overflow-hidden">
            <div className="container mx-auto px-4">
                <p className="text-center text-muted-foreground text-sm font-medium mb-8 uppercase tracking-widest">
                    Trusted by industry leaders
                </p>
                <div className="flex justify-center items-center gap-12 md:gap-24 flex-wrap opacity-50 grayscale hover:grayscale-0 transition-all duration-500">
                    {companies.map((company, i) => (
                        <motion.div
                            key={i}
                            initial={{ opacity: 0, y: 20 }}
                            whileInView={{ opacity: 1, y: 0 }}
                            transition={{ delay: i * 0.1, duration: 0.5 }}
                            viewport={{ once: true }}
                            className="text-xl font-bold font-mono tracking-tighter"
                        >
                            {company}
                        </motion.div>
                    ))}
                </div>
            </div>
        </section>
    )
}
