import { Hero } from "@/components/Hero"
import { TrustedBy } from "@/components/TrustedBy"
import { Features } from "@/components/Features"
import { Stats } from "@/components/Stats"
import { AppDownload } from "@/components/AppDownload"
import { Button } from "@swifttrack/shared-ui"

export default function Home() {
  return (
    <main>
      <Hero />
      <TrustedBy />
      <Features />
      <Stats />

      {/* Testimonial / Social Proof Section */}
      <section className="py-24 bg-white/85 border-y border-slate-200">
        <div className="container mx-auto px-4 text-center max-w-4xl">
          <h2 className="text-3xl md:text-4xl font-bold mb-12 bg-clip-text text-transparent bg-gradient-to-r from-slate-950 to-primary">Don&apos;t just take our word for it</h2>
          <blockquote className="text-2xl md:text-3xl font-light italic text-slate-700 mb-8 leading-relaxed">
            &ldquo;SwiftTrack revolutionized how we handle our last-mile deliveries. The AI dispatch alone saved us 30% in fuel costs in the first month.&rdquo;
          </blockquote>
          <div className="flex items-center justify-center gap-4">
            <div className="w-12 h-12 rounded-full bg-gradient-to-br from-primary to-cyan-500" />
            <div className="text-left">
              <div className="font-semibold text-slate-950">Sarah Jenkins</div>
              <div className="text-sm text-slate-500">Operations Director, FastLogistics</div>
            </div>
          </div>
        </div>
      </section>

      <AppDownload />

      {/* Footer CTA */}
      <section className="py-24 border-t border-slate-200 bg-gradient-to-r from-slate-950 via-blue-950 to-slate-950">
        <div className="container mx-auto px-4 text-center">
          <h2 className="text-3xl sm:text-4xl font-bold tracking-tight mb-8 bg-clip-text text-transparent bg-gradient-to-r from-white to-cyan-300">Ready to modernize your logistics?</h2>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Button size="lg" className="bg-primary hover:bg-primary/90 text-white h-14 px-8 text-lg rounded-full shadow-[0_0_30px_-5px_var(--color-primary)]">
              Get Started for Free
            </Button>
            <Button size="lg" variant="outline" className="h-14 px-8 text-lg rounded-full border-white/30 bg-white/10 text-white hover:bg-white/20">
              Contact Sales
            </Button>
          </div>
        </div>
      </section>
    </main>
  )
}
