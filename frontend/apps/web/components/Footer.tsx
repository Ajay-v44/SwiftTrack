import Link from "next/link"

export function Footer() {
    return (
        <footer className="py-12 border-t border-white/10 bg-black/40">
            <div className="container mx-auto px-4">
                <div className="grid grid-cols-2 md:grid-cols-4 gap-8 mb-12">
                    <div>
                        <h4 className="font-bold mb-4 text-white">Product</h4>
                        <ul className="space-y-2 text-sm text-muted-foreground">
                            <li><Link href="/solutions" className="hover:text-primary transition-colors">Features</Link></li>
                            <li><Link href="#" className="hover:text-primary transition-colors">Pricing</Link></li>
                            <li><Link href="#" className="hover:text-primary transition-colors">API</Link></li>
                            <li><Link href="#" className="hover:text-primary transition-colors">Integration</Link></li>
                        </ul>
                    </div>
                    <div>
                        <h4 className="font-bold mb-4 text-white">Company</h4>
                        <ul className="space-y-2 text-sm text-muted-foreground">
                            <li><Link href="/about" className="hover:text-primary transition-colors">About Us</Link></li>
                            <li><Link href="#" className="hover:text-primary transition-colors">Careers</Link></li>
                            <li><Link href="#" className="hover:text-primary transition-colors">Blog</Link></li>
                            <li><Link href="/contact" className="hover:text-primary transition-colors">Contact</Link></li>
                        </ul>
                    </div>
                    <div>
                        <h4 className="font-bold mb-4 text-white">Resources</h4>
                        <ul className="space-y-2 text-sm text-muted-foreground">
                            <li><Link href="#" className="hover:text-primary transition-colors">Documentation</Link></li>
                            <li><Link href="#" className="hover:text-primary transition-colors">Help Center</Link></li>
                            <li><Link href="#" className="hover:text-primary transition-colors">Community</Link></li>
                        </ul>
                    </div>
                    <div>
                        <h4 className="font-bold mb-4 text-white">Legal</h4>
                        <ul className="space-y-2 text-sm text-muted-foreground">
                            <li><Link href="#" className="hover:text-primary transition-colors">Privacy</Link></li>
                            <li><Link href="#" className="hover:text-primary transition-colors">Terms</Link></li>
                            <li><Link href="#" className="hover:text-primary transition-colors">Security</Link></li>
                        </ul>
                    </div>
                </div>
                <div className="flex flex-col md:flex-row justify-between items-center text-sm text-muted-foreground pt-8 border-t border-white/5">
                    <p>&copy; 2026 SwiftTrack. All rights reserved.</p>
                    <div className="flex gap-6 mt-4 md:mt-0">
                        <Link href="#" className="hover:text-white transition-colors">Twitter</Link>
                        <Link href="#" className="hover:text-white transition-colors">LinkedIn</Link>
                        <Link href="#" className="hover:text-white transition-colors">GitHub</Link>
                    </div>
                </div>
            </div>
        </footer>
    )
}
