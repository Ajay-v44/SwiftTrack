import { Button } from "@swifttrack/shared-ui"
import { Check } from "lucide-react"

export default function SolutionsPage() {
    return (
        <main className="pt-24 min-h-screen">
            <section className="py-20 container mx-auto px-4">
                <div className="text-center max-w-3xl mx-auto mb-20">
                    <h1 className="text-4xl md:text-6xl font-bold mb-6">Solutions for every scale</h1>
                    <p className="text-xl text-muted-foreground">Whether you're a local bakery or a global enterprise, we have the infrastructure to move your goods.</p>
                </div>

                <div className="grid lg:grid-cols-2 gap-16 items-center mb-32">
                    <div className="order-2 lg:order-1">
                        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-blue-500/10 text-blue-400 text-sm font-medium mb-6">
                            Hyperlocal Delivery
                        </div>
                        <h2 className="text-3xl md:text-4xl font-bold mb-6">Instant city-wide delivery</h2>
                        <p className="text-muted-foreground text-lg mb-8 leading-relaxed">
                            Connect with our fleet of 2-wheelers and 3-wheelers for sub-60 minute deliveries within your city. Perfect for food, pharmacy, and retail.
                        </p>
                        <ul className="space-y-4 mb-8">
                            {["Live tracking link for customers", "Proof of delivery with photos", "Insulated bags for food safety"].map((item, i) => (
                                <li key={i} className="flex items-center gap-3">
                                    <div className="w-6 h-6 rounded-full bg-primary/20 flex items-center justify-center text-primary"><Check className="w-4 h-4" /></div>
                                    <span>{item}</span>
                                </li>
                            ))}
                        </ul>
                        <Button>Start Shipping Locally</Button>
                    </div>
                    <div className="order-1 lg:order-2 h-[400px] rounded-3xl relative overflow-hidden flex items-center justify-center group border border-white/10">
                        <img
                            src="https://placehold.co/800x600/166534/FFF?text=Hyperlocal+Delivery+Fleet"
                            alt="Hyperlocal Delivery"
                            className="absolute inset-0 w-full h-full object-cover transition-transform duration-700 group-hover:scale-110 opacity-70"
                        />
                        <div className="absolute inset-0 bg-gradient-to-t from-black/80 to-transparent" />

                        <div className="relative z-10 glass-card p-6 rounded-2xl max-w-xs translate-y-4 group-hover:translate-y-0 transition-transform duration-500">
                            <div className="flex items-center justify-between mb-4">
                                <div className="font-bold text-white">Order #1024</div>
                                <div className="text-xs px-2 py-1 bg-green-500/20 text-green-400 rounded border border-green-500/20">In Transit</div>
                            </div>
                            <div className="h-2 bg-white/10 rounded-full overflow-hidden mb-2">
                                <div className="h-full w-2/3 bg-green-500" />
                            </div>
                            <div className="text-xs text-gray-300 flex justify-between">
                                <span>Pick up</span>
                                <span>15 mins to drop</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="grid lg:grid-cols-2 gap-16 items-center mb-32">
                    <div className="h-[400px] rounded-3xl relative overflow-hidden flex items-center justify-center group border border-white/10">
                        <img
                            src="https://placehold.co/800x600/6b21a8/FFF?text=Enterprise+freight+Trucks"
                            alt="Enterprise Fleet"
                            className="absolute inset-0 w-full h-full object-cover transition-transform duration-700 group-hover:scale-110 opacity-70"
                        />
                        <div className="absolute inset-0 bg-gradient-to-t from-black/80 to-transparent" />

                        <div className="relative z-10 glass-card p-8 rounded-2xl max-w-xs group-hover:scale-105 transition-transform duration-500">
                            <div className="font-mono text-sm mb-2 text-gray-300">Route Optimization</div>
                            <div className="text-3xl font-bold text-white mb-2">Saved 12%</div>
                            <div className="text-sm text-green-400 font-bold flex items-center gap-2">
                                <span className="text-lg">▼</span> Fuel Costs
                            </div>
                        </div>
                    </div>
                    <div>
                        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-purple-500/10 text-purple-400 text-sm font-medium mb-6">
                            Enterprise Logistics
                        </div>
                        <h2 className="text-3xl md:text-4xl font-bold mb-6">Inter-city & Freight</h2>
                        <p className="text-muted-foreground text-lg mb-8 leading-relaxed">
                            Full truckload (FTL) and Part truckload (PTL) solutions for moving heavy goods across the country.
                        </p>
                        <ul className="space-y-4 mb-8">
                            {["Dedicated Key Account Manager", "API Integration with your ERP", "GST Proficient Invoicing"].map((item, i) => (
                                <li key={i} className="flex items-center gap-3">
                                    <div className="w-6 h-6 rounded-full bg-primary/20 flex items-center justify-center text-primary"><Check className="w-4 h-4" /></div>
                                    <span>{item}</span>
                                </li>
                            ))}
                        </ul>
                        <Button>Contact Enterprise Sales</Button>
                    </div>
                </div>
            </section>
        </main>
    )
}
