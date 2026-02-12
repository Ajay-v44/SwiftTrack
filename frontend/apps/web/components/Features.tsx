"use client"

import { motion } from "framer-motion"
import { ShieldCheck, Zap, Truck, Globe, BarChart3, Users } from "lucide-react"
import { DriverNetwork, LiveMap, AnalyticsChart } from "@swifttrack/shared-ui"

const features = [
    {
        icon: Zap,
        title: "Unified Decision Engine",
        description: "AI/ML algorithms dynamically select the best delivery option—in-house fleet, marketplace drivers, or 3rd-party providers—based on cost, ETA, and performance.",
    },
    {
        icon: Globe,
        title: "Real-Time Infrastructure",
        description: "WebSocket-powered low-latency tracking with Redis backing. Monitor your entire fleet live on a single interactive map with timeline events.",
    },
    {
        icon: Users,
        title: "Driver Marketplace",
        description: "Access a pool of verified freelance drivers. AI-based scoring and risk evaluation ensures reliability for every delivery.",
    },
    {
        icon: ShieldCheck,
        title: "3rd-Party Integrations",
        description: "Plug-and-play adapters for major providers like Shadowfax, Dunzo, and Porter. Automatic status mirroring and multi-provider fallback flows.",
    },
    {
        icon: BarChart3,
        title: "Intelligent Analytics",
        description: "Deep insights into delivery times, costs, and efficiency. AI Chatbot allows you to query operations using natural language.",
    },
    {
        icon: Truck,
        title: "Dedicated Driver Apps",
        description: "Comprehensive apps for driver onboarding, order management, and route navigation with automated payout tracking.",
    },
]

export function Features() {
    return (
        <section id="features" className="py-24 relative dark bg-background">
            <div className="container mx-auto px-4">
                <div className="text-center max-w-3xl mx-auto mb-16">
                    <h2 className="text-3xl font-bold tracking-tight sm:text-4xl mb-4 bg-clip-text text-transparent bg-gradient-to-r from-white to-purple-300">
                        Powerful features for modern logistics
                    </h2>
                    <p className="text-lg text-muted-foreground">
                        Everything you need to orchestrate your deliveries, from order creation to final proof of delivery.
                    </p>
                </div>

                <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
                    {features.map((feature, i) => (
                        <motion.div
                            key={i}
                            initial={{ opacity: 0, y: 20 }}
                            whileInView={{ opacity: 1, y: 0 }}
                            transition={{ delay: i * 0.1, duration: 0.5 }}
                            viewport={{ once: true }}
                            className="group relative p-8 rounded-3xl border border-white/5 bg-white/[0.02] hover:bg-white/[0.04] transition-colors overflow-hidden"
                        >
                            <div className="absolute inset-0 bg-gradient-to-br from-primary/10 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity rounded-3xl" />

                            {/* Visualization Layer */}
                            {i === 0 && (
                                <div className="absolute top-0 right-0 w-full h-full opacity-0 group-hover:opacity-100 transition-opacity duration-700 pointer-events-none">
                                    <div className="absolute -top-10 -right-10 w-[120%] h-[120%] rotate-12 scale-110">
                                        {/* Using a simplified version or composition for background effect */}
                                    </div>
                                </div>
                            )}

                            {/* Specific Illustrations for key features */}
                            {i === 0 && (
                                <div className="absolute top-0 right-0 w-48 h-48 opacity-20 group-hover:opacity-100 transition-all duration-500 -mr-12 -mt-12 rounded-full overflow-hidden border border-white/10 bg-neutral-900/50">
                                    <div className="scale-75 origin-top-right">
                                        <DriverNetwork />
                                    </div>
                                </div>
                            )}
                            {i === 1 && (
                                <div className="absolute top-0 right-0 w-48 h-48 opacity-20 group-hover:opacity-100 transition-all duration-500 -mr-12 -mt-12 rounded-full overflow-hidden border border-white/10 bg-neutral-900/50">
                                    <div className="scale-75 origin-top-right">
                                        <LiveMap />
                                    </div>
                                </div>
                            )}
                            {i === 4 && (
                                <div className="absolute top-0 right-0 w-48 h-48 opacity-20 group-hover:opacity-100 transition-all duration-500 -mr-12 -mt-12 rounded-full overflow-hidden border border-white/10 bg-neutral-900/50">
                                    <div className="scale-75 origin-top-right">
                                        <AnalyticsChart />
                                    </div>
                                </div>
                            )}

                            <div className="relative z-10">
                                <div className="w-12 h-12 rounded-xl bg-primary/20 flex items-center justify-center mb-6 text-primary group-hover:scale-110 transition-transform duration-300 shadow-[0_0_15px_-3px_var(--color-primary)]">
                                    <feature.icon className="w-6 h-6" />
                                </div>
                                <h3 className="text-xl font-semibold mb-3 text-foreground">{feature.title}</h3>
                                <p className="text-muted-foreground leading-relaxed">
                                    {feature.description}
                                </p>
                            </div>
                        </motion.div>
                    ))}
                </div>
            </div>
        </section>
    )
}
