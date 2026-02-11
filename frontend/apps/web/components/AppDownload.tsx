"use client"

import { Button } from "@swifttrack/shared-ui"
import { motion } from "framer-motion"
import { Smartphone, Download, Star } from "lucide-react"

export function AppDownload() {
    return (
        <section className="py-24 relative overflow-hidden">
            <div className="container mx-auto px-4">
                <div className="bg-gradient-to-br from-primary/20 to-purple-900/20 rounded-[3rem] border border-white/10 p-12 md:p-24 relative overflow-hidden">
                    {/* Background decoration */}
                    <div className="absolute top-0 right-0 w-[500px] h-[500px] bg-primary/30 blur-[100px] rounded-full translate-x-1/2 -translate-y-1/2" />

                    <div className="grid lg:grid-cols-2 gap-12 items-center relative z-10">
                        <div className="text-left space-y-8">
                            <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-primary/10 border border-primary/20 text-primary text-sm font-medium">
                                <Star className="w-4 h-4 fill-primary" />
                                <span>#1 Rated Logistics App</span>
                            </div>

                            <h2 className="text-4xl md:text-5xl font-bold tracking-tight text-white leading-tight">
                                Manage your fleet <br />
                                <span className="text-primary bg-clip-text text-transparent bg-gradient-to-r from-primary to-purple-400">
                                    on the go.
                                </span>
                            </h2>

                            <p className="text-lg text-muted-foreground max-w-lg">
                                Download the SwiftTrack Driver and Fleet Manager apps to track shipments, assign orders, and communicate with your team from anywhere.
                            </p>

                            <div className="flex flex-col sm:flex-row gap-4">
                                <Button size="lg" className="h-14 px-8 text-lg rounded-xl bg-white text-black hover:bg-white/90">
                                    <Download className="mr-2 h-5 w-5" />
                                    App Store
                                </Button>
                                <Button size="lg" variant="outline" className="h-14 px-8 text-lg rounded-xl glass hover:bg-white/10">
                                    <Smartphone className="mr-2 h-5 w-5" />
                                    Play Store
                                </Button>
                            </div>

                            <div className="flex items-center gap-4 text-sm text-muted-foreground">
                                <div className="flex -space-x-2">
                                    <div className="w-8 h-8 rounded-full bg-gray-800 border-2 border-black" />
                                    <div className="w-8 h-8 rounded-full bg-gray-700 border-2 border-black" />
                                    <div className="w-8 h-8 rounded-full bg-gray-600 border-2 border-black" />
                                </div>
                                <span>Trusted by 50k+ drivers</span>
                            </div>
                        </div>

                        {/* Mobile Mockup Placeholder */}
                        <motion.div
                            initial={{ opacity: 0, y: 50 }}
                            whileInView={{ opacity: 1, y: 0 }}
                            transition={{ duration: 0.8 }}
                            className="relative hidden lg:block"
                        >
                            <div className="relative mx-auto w-[300px] h-[600px] bg-black border-[14px] border-gray-900 rounded-[3rem] shadow-2xl rotate-[-6deg] overflow-hidden">
                                <div className="absolute top-0 left-1/2 -translate-x-1/2 h-[25px] w-[120px] bg-gray-900 rounded-b-[1rem] z-20" />
                                <div className="w-full h-full bg-zinc-900 flex flex-col relative">
                                    {/* App UI Header */}
                                    <div className="p-6 pt-12 bg-zinc-900 z-10">
                                        <div className="h-4 w-8 bg-zinc-800 rounded mb-4" />
                                        <div className="h-8 w-32 bg-zinc-700 rounded mb-2" />
                                        <div className="h-4 w-48 bg-zinc-800 rounded" />
                                    </div>

                                    {/* Map Mockup */}
                                    <div className="flex-1 bg-zinc-800 relative opacity-50">
                                        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2">
                                            <div className="w-32 h-32 rounded-full border-4 border-primary/30 flex items-center justify-center animate-pulse">
                                                <div className="w-4 h-4 bg-primary rounded-full shadow-[0_0_20px_rgba(124,58,237,1)]" />
                                            </div>
                                        </div>
                                    </div>

                                    {/* Bottom Sheet */}
                                    <div className="h-1/3 bg-zinc-900 rounded-t-[2rem] p-6 absolute bottom-0 w-full z-10 border-t border-white/5">
                                        <div className="w-12 h-1 bg-zinc-700 rounded-full mx-auto mb-6" />
                                        <div className="space-y-4">
                                            <div className="h-12 w-full bg-primary/20 rounded-xl flex items-center px-4">
                                                <div className="w-8 h-8 rounded-full bg-primary/40 mr-3" />
                                                <div className="h-2 w-24 bg-primary/40 rounded" />
                                            </div>
                                            <div className="h-12 w-full bg-zinc-800 rounded-xl" />
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </motion.div>
                    </div>
                </div>
            </div>
        </section>
    )
}
