"use client"

import { Button, Input, HeroBackground } from "@swifttrack/shared-ui"
import { MapPin, Truck, ShieldCheck, Zap } from "lucide-react"

export function Hero() {
    return (
        <div className="relative pt-32 pb-20 lg:pt-48 lg:pb-32 overflow-hidden">
            {/* Background Gradients & Image */}
            {/* Background Gradients & Animation */}
            <HeroBackground />
            <div className="absolute inset-0 bg-gradient-to-b from-transparent via-background/50 to-background pointer-events-none" />

            <div className="absolute top-0 left-1/2 -translate-x-1/2 w-full h-full z-0 pointer-events-none">
                <div className="absolute top-[-10%] left-[-10%] w-[50%] h-[50%] bg-primary/20 blur-[120px] rounded-full mix-blend-screen animate-blob"></div>
                <div className="absolute top-[20%] right-[-10%] w-[40%] h-[40%] bg-purple-500/20 blur-[120px] rounded-full mix-blend-screen animate-blob animation-delay-2000"></div>
                <div className="absolute bottom-[-10%] left-[20%] w-[40%] h-[40%] bg-blue-500/20 blur-[120px] rounded-full mix-blend-screen animate-blob animation-delay-4000"></div>
            </div>

            <div className="container relative z-10 mx-auto px-4 text-center">
                <div className="inline-flex items-center rounded-full border border-primary/20 bg-primary/5 px-3 py-1 text-sm font-medium text-primary mb-8 backdrop-blur-sm">
                    <span className="flex h-2 w-2 rounded-full bg-primary mr-2 animate-pulse"></span>
                    Now Live: SwiftTrack Marketplace
                </div>

                <h1 className="text-4xl font-extrabold tracking-tight sm:text-6xl md:text-7xl lg:text-8xl mb-6 bg-clip-text text-transparent bg-gradient-to-b from-foreground to-foreground/50">
                    The Operating System for <br className="hidden md:block" />
                    <span className="bg-gradient-to-r from-primary to-purple-500 bg-clip-text text-transparent">Modern Logistics</span>
                </h1>

                <p className="mx-auto mt-6 max-w-2xl text-lg md:text-xl text-muted-foreground leading-relaxed">
                    Unify your fleet, 3rd-party providers, and freelance drivers in one AI-driven platform.
                    Deliver faster, cheaper, and more reliably.
                </p>

                <div className="mt-10 flex flex-col sm:flex-row items-center justify-center gap-4">
                    <div className="relative w-full max-w-sm sm:max-w-md">
                        <div className="absolute -inset-1 rounded-lg bg-gradient-to-r from-primary to-purple-600 opacity-30 blur transition group-hover:opacity-100"></div>
                        <div className="relative flex glass-card rounded-lg p-1.5 ring-1 ring-white/10">
                            <Input
                                placeholder="Enter Tracking ID (e.g. SWIFT123...)"
                                className="border-0 bg-transparent focus-visible:ring-0 text-foreground placeholder:text-muted-foreground/50"
                            />
                            <Button className="ml-1 bg-primary hover:bg-primary/90">
                                Track
                            </Button>
                        </div>
                    </div>
                    <span className="text-sm text-muted-foreground hidden sm:inline">or</span>
                    <Button size="lg" variant="outline" className="w-full sm:w-auto glass border-primary/20 hover:bg-primary/5 text-primary">
                        Request Demo
                    </Button>
                </div>

                {/* Floating Stats/Features */}
                <div className="mt-20 grid grid-cols-2 gap-4 md:grid-cols-4 lg:gap-8 max-w-5xl mx-auto">
                    {[
                        { label: "On-Time Delivery", value: "99.8%", icon: Zap },
                        { label: "Cost Reduction", value: "35%", icon: ShieldCheck },
                        { label: "Active Drivers", value: "15k+", icon: Truck },
                        { label: "Cities Covered", value: "250+", icon: MapPin },
                    ].map((stat, i) => (
                        <div key={i} className="glass-card p-6 rounded-2xl flex flex-col items-center justify-center text-center hover:scale-105 transition-transform duration-300">
                            <stat.icon className="h-8 w-8 text-primary mb-3" />
                            <div className="text-3xl font-bold tracking-tight text-foreground">{stat.value}</div>
                            <div className="text-sm text-muted-foreground mt-1">{stat.label}</div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    )
}
