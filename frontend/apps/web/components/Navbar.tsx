"use client"

import Link from "next/link"
import { useState } from "react"
import { Button, Sheet, SheetContent, SheetTrigger, Logo } from "@swifttrack/shared-ui"
import { Menu, ChevronRight, Package, User, LogOut, LayoutDashboard } from "lucide-react"
import { useAuthStore } from "@/store/useAuthStore"

export function Navbar() {
    const [isOpen, setIsOpen] = useState(false)
    const { user, logout } = useAuthStore()

    const navLinks = [
        { name: "Solutions", href: "/solutions" },
        { name: "Partners", href: "/partners" },
        { name: "Company", href: "/about" },
        { name: "Contact", href: "/contact" },
    ]

    return (
        <nav className="fixed w-full z-50 glass border-b border-white/10">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex items-center justify-between h-16">
                    <div className="flex items-center">
                        <div className="flex-shrink-0">
                            <Logo />
                        </div>
                        <div className="hidden md:block">
                            <div className="ml-10 flex items-baseline space-x-4">
                                {navLinks.map((link) => (
                                    <Link
                                        key={link.name}
                                        href={link.href}
                                        className="text-foreground/80 hover:text-primary px-3 py-2 rounded-md text-sm font-medium transition-colors"
                                    >
                                        {link.name}
                                    </Link>
                                ))}
                            </div>
                        </div>
                    </div>
                    <div className="hidden md:block">
                        <div className="flex items-center gap-4">
                            <Link href="/track" className="text-sm font-medium text-foreground/80 hover:text-primary">
                                Track Order
                            </Link>
                            
                            {user ? (
                                <div className="flex items-center gap-4">
                                    <Link href="/dashboard">
                                        <Button variant="ghost" className="text-foreground/80 hover:text-primary gap-2">
                                            <LayoutDashboard className="h-4 w-4" />
                                            Dashboard
                                        </Button>
                                    </Link>
                                    <Link href="/profile">
                                        <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-primary/10 border border-primary/20 hover:bg-primary/20 transition-colors cursor-pointer">
                                            <User className="h-4 w-4 text-primary" />
                                            <span className="text-sm font-medium text-primary">{user.name}</span>
                                        </div>
                                    </Link>
                                    <Button variant="ghost" size="icon" onClick={logout} className="text-muted-foreground hover:text-destructive">
                                        <LogOut className="h-4 w-4" />
                                    </Button>
                                </div>
                            ) : (
                                <>
                                    <Link href="/login">
                                        <Button variant="ghost" className="text-foreground/80 hover:text-primary">
                                            Sign In
                                        </Button>
                                    </Link>
                                    <Link href="/register">
                                        <Button className="bg-primary hover:bg-primary/90 text-primary-foreground shadow-lg shadow-primary/20">
                                            Get Started <ChevronRight className="ml-2 h-4 w-4" />
                                        </Button>
                                    </Link>
                                </>
                            )}
                        </div>
                    </div>
                    <div className="-mr-2 flex md:hidden">
                        <Sheet open={isOpen} onOpenChange={setIsOpen}>
                            <SheetTrigger asChild>
                                <Button variant="ghost" size="icon">
                                    <Menu className="h-6 w-6" />
                                    <span className="sr-only">Open main menu</span>
                                </Button>
                            </SheetTrigger>
                            <SheetContent side="right" className="w-[300px] sm:w-[400px] glass p-6">
                                <nav className="flex flex-col h-full mt-8">
                                    <div className="flex flex-col gap-6">
                                        {navLinks.map((link) => (
                                            <Link
                                                key={link.name}
                                                href={link.href}
                                                onClick={() => setIsOpen(false)}
                                                className="text-xl font-medium text-foreground py-2 hover:text-primary transition-colors hover:pl-2"
                                            >
                                                {link.name}
                                            </Link>
                                        ))}
                                    </div>
                                    <div className="h-px bg-border my-6"></div>
                                    <div className="flex flex-col gap-4 mt-auto mb-8">
                                        <Link href="/track" onClick={() => setIsOpen(false)}>
                                            <Button variant="outline" className="w-full justify-start h-12 text-base">
                                                <Package className="mr-2 h-5 w-5" /> Track Order
                                            </Button>
                                        </Link>
                                        {user ? (
                                            <>
                                                <Link href="/dashboard" onClick={() => setIsOpen(false)}>
                                                    <Button variant="outline" className="w-full justify-start h-12 text-base">
                                                        <LayoutDashboard className="mr-2 h-5 w-5" /> Dashboard
                                                    </Button>
                                                </Link>
                                                <Link href="/profile" onClick={() => setIsOpen(false)}>
                                                    <Button variant="outline" className="w-full justify-start h-12 text-base">
                                                        <User className="mr-2 h-5 w-5" /> Profile
                                                    </Button>
                                                </Link>
                                                <Button variant="ghost" className="w-full justify-start h-12 text-base text-destructive" onClick={() => { logout(); setIsOpen(false); }}>
                                                    <LogOut className="mr-2 h-5 w-5" /> Sign Out
                                                </Button>
                                            </>
                                        ) : (
                                            <>
                                                <Link href="/login" onClick={() => setIsOpen(false)}>
                                                    <Button variant="ghost" className="w-full justify-start h-12 text-base">
                                                        Sign In
                                                    </Button>
                                                </Link>
                                                <Link href="/register" onClick={() => setIsOpen(false)}>
                                                    <Button className="w-full h-12 text-base shadow-lg shadow-primary/20">
                                                        Get Started
                                                    </Button>
                                                </Link>
                                            </>
                                        )}
                                    </div>
                                </nav>
                            </SheetContent>
                        </Sheet>
                    </div>
                </div>
            </div>
        </nav>
    )
}
