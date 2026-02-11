"use client"

import { Button } from "@swifttrack/shared-ui"
import { Check, Truck, Bike, Car } from "lucide-react"

export default function PartnersPage() {
    return (
        <main className="pt-24 min-h-screen">
            <section className="py-20 container mx-auto px-4 text-center">
                <h1 className="text-4xl md:text-6xl font-bold mb-6">Drive with SwiftTrack</h1>
                <p className="text-xl text-muted-foreground max-w-2xl mx-auto mb-12">
                    Earn more with flexible hours, instant payouts, and a steady stream of orders. Join India's fastest growing logistics network.
                </p>
                <div className="flex justify-center gap-4 mb-20">
                    <Button size="lg" className="h-14 px-8 text-lg rounded-full">Sign Up to Drive</Button>
                    <Button size="lg" variant="outline" className="h-14 px-8 text-lg rounded-full glass">Download App</Button>
                </div>

                <div className="grid md:grid-cols-3 gap-8 text-left mb-32">
                    {[
                        {
                            icon: Bike,
                            title: "2-Wheelers",
                            desc: "Perfect for food, grocery, and small parcel deliveries within the city.",
                            earning: "Earn up to ₹25,000/month"
                        },
                        {
                            icon: Car,
                            title: "3-Wheelers & Eeco",
                            desc: "Ideal for furniture, appliances, and bulk orders.",
                            earning: "Earn up to ₹45,000/month"
                        },
                        {
                            icon: Truck,
                            title: "Trucks (Tata Ace/407)",
                            desc: "For heavy goods and inter-city transport.",
                            earning: "Earn up to ₹1,00,000/month"
                        }

                    ].map((type, i) => (
                        <div key={i} className="p-8 rounded-3xl bg-white/[0.03] border border-white/5 hover:border-primary/50 transition-colors">
                            <div className="w-14 h-14 rounded-2xl bg-primary/20 flex items-center justify-center text-primary mb-6">
                                <type.icon className="w-8 h-8" />
                            </div>
                            <h3 className="text-xl font-bold mb-2">{type.title}</h3>
                            <p className="text-muted-foreground mb-4">{type.desc}</p>
                            <div className="text-green-400 font-semibold bg-green-400/10 inline-block px-3 py-1 rounded-full text-sm">
                                {type.earning}
                            </div>
                        </div>
                    ))}
                </div>

                <div className="bg-gradient-to-r from-primary/20 to-purple-900/20 rounded-[3rem] p-12 md:p-20 relative overflow-hidden text-left flex flex-col md:flex-row items-center justify-between gap-12 group">
                    {/* Background Image */}
                    <img
                        src="https://placehold.co/1200x600/111827/FFF?text=Join+Our+Fleet"
                        alt="Partner Background"
                        className="absolute inset-0 w-full h-full object-cover opacity-20 transition-transform duration-700 group-hover:scale-105"
                    />
                    <div className="absolute inset-0 bg-gradient-to-r from-background via-background/80 to-transparent" />

                    <div className="relative z-10 max-w-xl">
                        <h2 className="text-3xl md:text-4xl font-bold mb-6">Why Partner with Us?</h2>
                        <ul className="space-y-4">
                            {[
                                "Zero onboarding fees",
                                "Daily payouts directly to your bank",
                                "Accidental insurance coverage up to ₹5 Lakhs",
                                "24/7 Support helpline"
                            ].map((item, i) => (
                                <li key={i} className="flex items-center gap-3">
                                    <div className="w-6 h-6 rounded-full bg-white/20 flex items-center justify-center backdrop-blur-sm"><Check className="w-4 h-4" /></div>
                                    <span className="text-lg">{item}</span>
                                </li>
                            ))}
                        </ul>
                    </div>
                    <div className="relative z-10 bg-card p-8 rounded-3xl border border-white/10 w-full max-w-md shadow-2xl">
                        <h3 className="text-xl font-bold mb-6">Estimate your earnings</h3>
                        <div className="space-y-6">
                            <div>
                                <label className="text-sm text-muted-foreground block mb-2">City</label>
                                <select className="w-full bg-white/5 border border-white/10 rounded-xl h-12 px-4">
                                    <option>Bangalore</option>
                                    <option>Mumbai</option>
                                    <option>Delhi</option>
                                </select>
                            </div>
                            <div>
                                <label className="text-sm text-muted-foreground block mb-2">Vehicle Type</label>
                                <select className="w-full bg-white/5 border border-white/10 rounded-xl h-12 px-4">
                                    <option>Bike</option>
                                    <option>3-Wheeler</option>
                                    <option>Tata Ace</option>
                                </select>
                            </div>
                            <div className="pt-4 border-t border-white/10">
                                <div className="text-sm text-muted-foreground mb-1">Potential Monthly Earnings</div>
                                <div className="text-3xl font-bold text-primary">₹ 28,500</div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>
        </main>
    )
}
