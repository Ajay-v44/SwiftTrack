"use client"

import { Button, Input, Textarea } from "@swifttrack/shared-ui"
import { Mail, MapPin, Phone } from "lucide-react"

export default function ContactPage() {
    return (
        <main className="pt-24 min-h-screen">
            <div className="container mx-auto px-4 py-12">
                <div className="grid lg:grid-cols-2 gap-16">
                    <div>
                        <h1 className="text-4xl font-bold mb-6">Get in touch</h1>
                        <p className="text-muted-foreground text-lg mb-12">
                            Have questions about our enterprise solutions or need support? We're here to help.
                        </p>

                        <div className="space-y-8">
                            <div className="flex gap-4">
                                <div className="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center text-primary shrink-0">
                                    <Mail className="w-5 h-5" />
                                </div>
                                <div>
                                    <h3 className="font-bold mb-1">Email</h3>
                                    <p className="text-muted-foreground">support@swifttrack.com</p>
                                    <p className="text-muted-foreground">sales@swifttrack.com</p>
                                </div>
                            </div>
                            <div className="flex gap-4">
                                <div className="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center text-primary shrink-0">
                                    <Phone className="w-5 h-5" />
                                </div>
                                <div>
                                    <h3 className="font-bold mb-1">Phone</h3>
                                    <p className="text-muted-foreground">+91 98765 43210</p>
                                    <p className="text-muted-foreground">Mon-Fri 9am-6pm IST</p>
                                </div>
                            </div>
                            <div className="flex gap-4">
                                <div className="w-12 h-12 rounded-full bg-primary/10 flex items-center justify-center text-primary shrink-0">
                                    <MapPin className="w-5 h-5" />
                                </div>
                                <div>
                                    <h3 className="font-bold mb-1">Office</h3>
                                    <p className="text-muted-foreground">
                                        SwiftTrack Logistics Pvt Ltd<br />
                                        123 Tech Park, Indiranagar<br />
                                        Bangalore, Karnataka 560038
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className="bg-card border border-white/5 rounded-3xl p-8 shadow-xl">
                        <form className="space-y-6">
                            <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                    <label className="text-sm font-medium">First Name</label>
                                    <Input placeholder="John" className="bg-white/5" />
                                </div>
                                <div className="space-y-2">
                                    <label className="text-sm font-medium">Last Name</label>
                                    <Input placeholder="Doe" className="bg-white/5" />
                                </div>
                            </div>
                            <div className="space-y-2">
                                <label className="text-sm font-medium">Email</label>
                                <Input placeholder="john@example.com" type="email" className="bg-white/5" />
                            </div>
                            <div className="space-y-2">
                                <label className="text-sm font-medium">Message</label>
                                <Textarea placeholder="How can we help you?" className="bg-white/5 min-h-[150px]" />
                            </div>
                            <Button size="lg" className="w-full">Send Message</Button>
                        </form>
                    </div>
                </div>
            </div>
        </main>
    )
}
