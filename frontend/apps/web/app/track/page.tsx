"use client"

import { Button, Input } from "@swifttrack/shared-ui"
import { Search } from "lucide-react"
import { useState } from "react"
import { motion } from "framer-motion"

export default function TrackPage() {
    const [trackingId, setTrackingId] = useState("")
    const [status, setStatus] = useState<"idle" | "searching" | "found">("idle")

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault()
        setStatus("searching")
        // Simulate API call
        setTimeout(() => {
            setStatus("found")
        }, 1500)
    }

    return (
        <main className="pt-24 min-h-screen">
            <div className="container mx-auto px-4 py-12">
                <div className="max-w-2xl mx-auto text-center mb-12">
                    <h1 className="text-3xl font-bold mb-4">Track your shipment</h1>
                    <p className="text-muted-foreground mb-8">Enter your tracking ID (e.g., SW-123456789)</p>

                    <form onSubmit={handleSearch} className="flex gap-2 relative z-10">
                        <div className="relative flex-1">
                            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground h-5 w-5" />
                            <Input
                                placeholder="Tracking ID"
                                className="pl-10 h-14 bg-white/5 border-white/10 text-lg rounded-xl focus-visible:ring-primary/50"
                                value={trackingId}
                                onChange={(e: React.ChangeEvent<HTMLInputElement>) => setTrackingId(e.target.value)}
                            />
                        </div>
                        <Button type="submit" size="lg" className="h-14 px-8 rounded-xl bg-primary hover:bg-primary/90">
                            Track
                        </Button>
                    </form>
                </div>

                {status === "searching" && (
                    <div className="flex justify-center py-12">
                        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary"></div>
                    </div>
                )}

                {status === "found" && (
                    <motion.div
                        initial={{ opacity: 0, y: 20 }}
                        animate={{ opacity: 1, y: 0 }}
                        className="max-w-3xl mx-auto bg-card border border-white/5 rounded-3xl p-8 overflow-hidden relative"
                    >
                        <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 pb-8 border-b border-white/5">
                            <div>
                                <div className="text-sm text-muted-foreground mb-1">Expected Delivery</div>
                                <div className="text-2xl font-bold text-green-400">Today, 2:30 PM</div>
                            </div>
                            <div className="mt-4 md:mt-0 text-right">
                                <div className="text-sm text-muted-foreground mb-1">Tracking ID</div>
                                <div className="font-mono font-medium">{trackingId || "SW-82930192"}</div>
                            </div>
                        </div>

                        {/* Timeline */}
                        <div className="space-y-8 relative pl-8 border-l-2 border-white/10 ml-4">
                            {[
                                { time: "1:45 PM", status: "Out for Delivery", location: "Bangalore Hub", active: true },
                                { time: "10:30 AM", status: "Arrived at Sort Facility", location: "Bangalore Hub", active: false },
                                { time: "Yesterday, 8:00 PM", status: "In Transit", location: "Mumbai Gateway", active: false },
                                { time: "Yesterday, 4:00 PM", status: "Picked Up", location: "Seller Warehouse", active: false },
                            ].map((step, i) => (
                                <div key={i} className="relative">
                                    <div className={`absolute -left-[41px] top-1 h-5 w-5 rounded-full border-4 border-background ${step.active ? "bg-green-500 animate-pulse" : "bg-white/20"}`} />
                                    <div className={`${step.active ? "text-white" : "text-muted-foreground"}`}>
                                        <div className="font-bold text-lg">{step.status}</div>
                                        <div className="text-sm text-muted-foreground/80">{step.location} • {step.time}</div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </motion.div>
                )}
            </div>
        </main>
    )
}
