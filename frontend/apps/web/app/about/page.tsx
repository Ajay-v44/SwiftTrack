import { Button } from "@swifttrack/shared-ui"
import { Users, Target, Rocket, Heart } from "lucide-react"

export default function AboutPage() {
    return (
        <main className="pt-24 min-h-screen">
            {/* Hero */}
            <section className="relative py-24 overflow-hidden">
                <div className="absolute inset-0 pointer-events-none">
                    <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[1000px] h-[600px] bg-primary/10 blur-[120px] rounded-full mix-blend-screen" />
                </div>

                <div className="container mx-auto px-4 relative z-10 text-center max-w-4xl">
                    <h1 className="text-5xl md:text-7xl font-bold tracking-tight mb-8 bg-clip-text text-transparent bg-gradient-to-r from-white to-white/60">
                        Revolutionizing Logistics
                    </h1>
                    <p className="text-xl text-muted-foreground leading-relaxed">
                        SwiftTrack was born from a simple mission: to build the operating system for modern commerce. We connect businesses, drivers, and customers through intelligent, real-time technology.
                    </p>
                </div>
            </section>

            {/* Values Grid */}
            <section className="py-24 border-y border-white/5 bg-white/[0.02]">
                <div className="container mx-auto px-4">
                    <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-8">
                        {[
                            { icon: Target, title: "Precision", desc: "We obsess over every detail, ensuring 99.9% accuracy in ETAs and dispatch." },
                            { icon: Users, title: "Community", desc: "Empowering 50,000+ drivers with fair wages and flexible work." },
                            { icon: Rocket, title: "Innovation", desc: "Pushing the boundaries of what's possible with AI and autonomous tech." },
                            { icon: Heart, title: "Customer Obsession", desc: "We build for trust. Your deliveries are our promise." }
                        ].map((item, i) => (
                            <div key={i} className="p-8 rounded-3xl bg-white/[0.03] border border-white/5 hover:border-primary/30 transition-colors group">
                                <div className="w-12 h-12 rounded-xl bg-primary/10 flex items-center justify-center mb-6 text-primary group-hover:scale-110 transition-transform">
                                    <item.icon className="w-6 h-6" />
                                </div>
                                <h3 className="text-xl font-bold mb-3">{item.title}</h3>
                                <p className="text-muted-foreground">{item.desc}</p>
                            </div>
                        ))}
                    </div>
                </div>
            </section>

            {/* Team Section (inspired by uEngage) */}
            <section className="py-24">
                <div className="container mx-auto px-4">
                    <div className="text-center mb-16">
                        <h2 className="text-3xl font-bold mb-4">Meet the Builders</h2>
                        <p className="text-muted-foreground">The diverse team of engineers, designers, and operators behind SwiftTrack.</p>
                    </div>

                    <div className="grid grid-cols-2 md:grid-cols-4 gap-8">
                        {[
                            { name: "Alex Chen", role: "CEO & Founder", color: "bg-blue-500" },
                            { name: "Sarah Miller", role: "CTO", color: "bg-purple-500" },
                            { name: "David Kim", role: "Head of Operations", color: "bg-emerald-500" },
                            { name: "Priya Patel", role: "Lead Product Designer", color: "bg-pink-500" },
                        ].map((member, i) => (
                            <div key={i} className="text-center group">
                                <div className={`w-full aspect-square rounded-3xl mb-6 ${member.color}/20 flex items-center justify-center overflow-hidden relative shadow-lg`}>
                                    <img
                                        src={`https://ui-avatars.com/api/?name=${member.name.replace(' ', '+')}&background=random&color=fff&size=512`}
                                        alt={member.name}
                                        className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110 grayscale group-hover:grayscale-0"
                                    />
                                    <div className={`absolute inset-0 ${member.color}/10 group-hover:opacity-0 transition-opacity`} />
                                </div>
                                <h3 className="text-lg font-bold">{member.name}</h3>
                                <p className="text-sm text-primary">{member.role}</p>
                            </div>
                        ))}
                    </div>
                </div>
            </section>

            {/* CTA */}
            <section className="py-24 border-t border-white/5">
                <div className="container mx-auto px-4 text-center">
                    <h2 className="text-3xl font-bold mb-8">Join our mission</h2>
                    <Button size="lg" className="rounded-full px-8">View Careers</Button>
                </div>
            </section>
        </main>
    )
}
