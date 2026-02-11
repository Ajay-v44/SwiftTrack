"use client"

import { motion } from "framer-motion"
import { ShieldCheck, Zap, Truck, Globe, BarChart3, Users } from "lucide-react"

const features = [
    {
        icon: Zap,
        title: "AI-Driven Dispatch",
        description: "Our algorithms automatically assign orders to the best-suited drivers based on proximity, vehicle type, and historical performance.",
    },
    {
        icon: Globe,
        title: "Real-Time Tracking",
        description: "Give your customers peace of mind with live tracking links. Monitor your entire fleet on a single, interactive map.",
    },
    {
        icon: BarChart3,
        title: "Advanced Analytics",
        description: "Gain deep insights into your operations with custom reports on delivery times, cost per mile, and driver efficiency.",
    },
    {
        icon: ShieldCheck,
        title: "Secure & Reliable",
        description: "Enterprise-grade security for your data. 99.99% uptime guarantee ensures your operations never stop.",
    },
    {
        icon: Truck,
        title: "Fleet Management",
        description: "Manage vehicle maintenance, fuel costs, and driver documents all in one place.",
    },
    {
        icon: Users,
        title: "Driver App",
        description: "A dedicated app for drivers to manage orders, navigate routes, and track earnings seamlessly.",
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

                            {/* Decorative Image for first 3 items */}
                            {i < 3 && (
                                <div className="absolute top-0 right-0 w-32 h-32 opacity-10 group-hover:opacity-20 transition-opacity -mr-8 -mt-8 rounded-full overflow-hidden">
                                    <img src={`https://placehold.co/200x200/4c1d95/FFF?text=Feature+${i + 1}`} alt="" className="w-full h-full object-cover" />
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
