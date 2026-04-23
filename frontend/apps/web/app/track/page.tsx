"use client"

import { Button, Input } from "@swifttrack/shared-ui"
import { Search, Package, MapPin, Clock, CheckCircle2, AlertCircle, ArrowRight } from "lucide-react"
import { useState, useEffect, Suspense } from "react"
import { motion, AnimatePresence } from "framer-motion"
import { fetchPublicOrderTrackingService } from "@swifttrack/services"
import { TenantOrderTrackingResponse } from "@swifttrack/types"
import { useSearchParams } from "next/navigation"

function TrackContent() {
    const searchParams = useSearchParams()
    const [trackingId, setTrackingId] = useState(searchParams.get("id") || "")
    const [status, setStatus] = useState<"idle" | "searching" | "found" | "error">("idle")
    const [trackingData, setTrackingData] = useState<TenantOrderTrackingResponse | null>(null)
    const [error, setError] = useState<string | null>(null)

    const handleSearch = async (e?: React.FormEvent) => {
        if (e) e.preventDefault()
        if (!trackingId.trim()) return

        setStatus("searching")
        setError(null)

        try {
            const data = await fetchPublicOrderTrackingService(trackingId)
            setTrackingData(data)
            setStatus("found")
        } catch (err) {
            console.error("Tracking failed", err)
            setError("We couldn't find a shipment with that tracking ID. Please check and try again.")
            setStatus("error")
        }
    }

    useEffect(() => {
        const id = searchParams.get("id")
        if (id) {
            setTrackingId(id)
            // We can't call handleSearch directly here because it uses state from the current render
            // Instead, we'll trigger it via a separate effect or just call it with the id
            const triggerSearch = async (tid: string) => {
                setStatus("searching")
                setError(null)
                try {
                    const data = await fetchPublicOrderTrackingService(tid)
                    setTrackingData(data)
                    setStatus("found")
                } catch (err) {
                    setError("We couldn't find a shipment with that tracking ID.")
                    setStatus("error")
                }
            }
            triggerSearch(id)
        }
    }, [searchParams])

    return (
        <div className="container mx-auto px-4 py-12 relative">
            {/* Background Glow */}
            <div className="absolute top-0 left-1/2 -translate-x-1/2 w-full h-[500px] bg-primary/5 blur-[120px] rounded-full pointer-events-none" />

            <div className="max-w-2xl mx-auto text-center mb-12 relative z-10">
                <motion.div
                    initial={{ opacity: 0, y: -20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.5 }}
                >
                    <h1 className="text-4xl md:text-5xl font-bold mb-4 bg-clip-text text-transparent bg-gradient-to-r from-white to-white/60">
                        Track your shipment
                    </h1>
                    <p className="text-muted-foreground text-lg mb-8">
                        Real-time updates for your SwiftTrack deliveries
                    </p>
                </motion.div>

                <form onSubmit={handleSearch} className="flex gap-3 p-2 bg-white/5 border border-white/10 rounded-2xl backdrop-blur-xl focus-within:border-primary/50 transition-colors">
                    <div className="relative flex-1">
                        <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-muted-foreground h-5 w-5" />
                        <Input
                            placeholder="Enter Tracking ID..."
                            className="pl-12 h-12 bg-transparent border-0 text-lg focus-visible:ring-0 placeholder:text-muted-foreground/30"
                            value={trackingId}
                            onChange={(e) => setTrackingId(e.target.value)}
                        />
                    </div>
                    <Button 
                        type="submit" 
                        size="lg" 
                        className="h-12 px-8 rounded-xl bg-primary hover:bg-primary/90 text-white font-semibold shadow-[0_0_20px_-5px_rgba(var(--primary-rgb),0.5)]"
                        disabled={status === "searching"}
                    >
                        {status === "searching" ? "Searching..." : "Track"}
                    </Button>
                </form>
            </div>

            <AnimatePresence mode="wait">
                {status === "searching" && (
                    <motion.div 
                        key="loading"
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        exit={{ opacity: 0 }}
                        className="flex flex-col items-center justify-center py-20"
                    >
                        <div className="relative h-16 w-16">
                            <div className="absolute inset-0 rounded-full border-4 border-primary/20" />
                            <div className="absolute inset-0 rounded-full border-4 border-primary border-t-transparent animate-spin" />
                        </div>
                        <p className="mt-4 text-muted-foreground animate-pulse">Locating your package...</p>
                    </motion.div>
                )}

                {status === "error" && (
                    <motion.div
                        key="error"
                        initial={{ opacity: 0, scale: 0.95 }}
                        animate={{ opacity: 1, scale: 1 }}
                        className="max-w-md mx-auto p-6 bg-red-500/10 border border-red-500/20 rounded-2xl text-center"
                    >
                        <AlertCircle className="h-12 w-12 text-red-500 mx-auto mb-4" />
                        <h3 className="text-xl font-semibold text-white mb-2">Shipment Not Found</h3>
                        <p className="text-red-200/70">{error}</p>
                    </motion.div>
                )}

                {status === "found" && trackingData && (
                    <motion.div
                        key="results"
                        initial={{ opacity: 0, y: 20 }}
                        animate={{ opacity: 1, y: 0 }}
                        className="max-w-4xl mx-auto space-y-6"
                    >
                        {/* Summary Card */}
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                            <div className="md:col-span-2 bg-white/5 border border-white/10 rounded-3xl p-8 backdrop-blur-sm">
                                <div className="flex justify-between items-start mb-8">
                                    <div>
                                        <div className="text-sm text-muted-foreground uppercase tracking-wider font-semibold mb-1">Status</div>
                                        <div className="flex items-center gap-2">
                                            <div className="h-2 w-2 rounded-full bg-green-500 animate-pulse" />
                                            <div className="text-2xl font-bold text-white">{trackingData.trackingStatus}</div>
                                        </div>
                                    </div>
                                    <div className="text-right">
                                        <div className="text-sm text-muted-foreground uppercase tracking-wider font-semibold mb-1">Tracking ID</div>
                                        <div className="font-mono text-lg text-white/80">{trackingId}</div>
                                    </div>
                                </div>

                                <div className="grid grid-cols-2 gap-8 py-6 border-y border-white/5">
                                    <div>
                                        <div className="flex items-center gap-2 text-muted-foreground text-sm mb-2">
                                            <MapPin className="h-4 w-4" /> From
                                        </div>
                                        <div className="font-semibold">Origin Hub</div>
                                    </div>
                                    <div className="text-right">
                                        <div className="flex items-center justify-end gap-2 text-muted-foreground text-sm mb-2">
                                            To <MapPin className="h-4 w-4" />
                                        </div>
                                        <div className="font-semibold">Destination</div>
                                    </div>
                                </div>

                                <div className="mt-8 flex items-center justify-between">
                                    <div className="flex items-center gap-3">
                                        <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center">
                                            <Package className="h-5 w-5 text-primary" />
                                        </div>
                                        <div>
                                            <div className="text-xs text-muted-foreground">Carrier</div>
                                            <div className="font-medium">SwiftTrack Express</div>
                                        </div>
                                    </div>
                                    <div className="flex items-center gap-2 text-primary font-medium hover:gap-3 transition-all cursor-pointer">
                                        View Order Details <ArrowRight className="h-4 w-4" />
                                    </div>
                                </div>
                            </div>

                            <div className="bg-gradient-to-br from-primary/20 to-purple-500/10 border border-primary/20 rounded-3xl p-8 flex flex-col justify-between">
                                <div>
                                    <Clock className="h-8 w-8 text-primary mb-4" />
                                    <div className="text-sm text-primary font-semibold uppercase tracking-wider mb-1">Estimated Arrival</div>
                                    <div className="text-3xl font-bold">Today</div>
                                    <div className="text-lg text-white/60">by 6:00 PM</div>
                                </div>
                                <div className="mt-8 pt-6 border-t border-white/10">
                                    <div className="text-sm text-muted-foreground mb-4">Current Location</div>
                                    <div className="flex items-center gap-2 font-medium">
                                        <div className="h-2 w-2 rounded-full bg-primary" />
                                        {trackingData.currentLocation?.status || "In Transit"}
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* Timeline */}
                        <div className="bg-white/5 border border-white/10 rounded-3xl p-8 backdrop-blur-sm">
                            <h3 className="text-xl font-bold mb-8 flex items-center gap-2">
                                <Clock className="h-5 w-5 text-primary" /> Shipment History
                            </h3>
                            <div className="space-y-0 relative">
                                {trackingData.trackingHistory.map((step, i) => (
                                    <div key={i} className="relative pl-10 pb-10 last:pb-0 group">
                                        {i !== trackingData.trackingHistory.length - 1 && (
                                            <div className="absolute left-[11px] top-7 bottom-0 w-[2px] bg-gradient-to-b from-primary to-white/5" />
                                        )}
                                        <div className={`absolute left-0 top-1 h-6 w-6 rounded-full flex items-center justify-center z-10 transition-transform group-hover:scale-110 ${i === 0 ? "bg-primary shadow-[0_0_15px_rgba(var(--primary-rgb),0.5)]" : "bg-white/10"}`}>
                                            {i === 0 ? (
                                                <CheckCircle2 className="h-4 w-4 text-white" />
                                            ) : (
                                                <div className="h-2 w-2 rounded-full bg-white/40" />
                                            )}
                                        </div>
                                        <div className="flex flex-col md:flex-row md:items-center justify-between gap-2">
                                            <div>
                                                <div className={`text-lg font-bold ${i === 0 ? "text-white" : "text-white/60"}`}>
                                                    {step.status}
                                                </div>
                                                <div className="text-muted-foreground/80 flex items-center gap-2">
                                                    <MapPin className="h-3 w-3" /> {step.description?.split("-").pop()?.trim() || "In Transit"}
                                                </div>
                                            </div>
                                            <div className="text-sm font-mono text-muted-foreground bg-white/5 px-3 py-1 rounded-full border border-white/5">
                                                {step.eventTime ? new Date(step.eventTime).toLocaleString() : new Date(step.createdAt!).toLocaleString()}
                                            </div>
                                        </div>
                                        {step.description && (
                                            <p className="mt-2 text-sm text-muted-foreground leading-relaxed max-w-xl">
                                                {step.description}
                                            </p>
                                        )}
                                    </div>
                                ))}
                            </div>
                        </div>
                    </motion.div>
                )}
            </AnimatePresence>
        </div>
    )
}

export default function TrackPage() {
    return (
        <main className="pt-24 min-h-screen bg-[#05070A] text-white">
            <Suspense fallback={<div className="flex items-center justify-center min-h-screen">Loading...</div>}>
                <TrackContent />
            </Suspense>
        </main>
    )
}
