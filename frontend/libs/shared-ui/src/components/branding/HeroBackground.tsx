"use client"

import { motion } from "framer-motion"

export function HeroBackground() {
    return (
        <div className="absolute inset-0 z-0 overflow-hidden bg-neutral-950">
            {/* Grid Pattern */}
            <div
                className="absolute inset-0 opacity-[0.15]"
                style={{
                    backgroundImage: `linear-gradient(to right, #4f4f4f 1px, transparent 1px),
                    linear-gradient(to bottom, #4f4f4f 1px, transparent 1px)`,
                    backgroundSize: '40px 40px'
                }}
            />

            {/* Radial Gradient overlay */}
            <div className="absolute inset-0 bg-gradient-to-b from-transparent via-neutral-950/80 to-neutral-950" />

            {/* Glowing Orbs/Nodes */}
            <div className="absolute inset-0">
                {[...Array(5)].map((_, i) => (
                    <motion.div
                        key={i}
                        className="absolute rounded-full mix-blend-screen filter blur-[80px]"
                        animate={{
                            x: [Math.random() * 100, Math.random() * -100, Math.random() * 100],
                            y: [Math.random() * 100, Math.random() * -100, Math.random() * 100],
                            scale: [1, 1.2, 1],
                            opacity: [0.3, 0.6, 0.3],
                        }}
                        transition={{
                            duration: 10 + Math.random() * 10,
                            repeat: Infinity,
                            ease: "linear",
                        }}
                        style={{
                            left: `${20 + Math.random() * 60}%`,
                            top: `${20 + Math.random() * 60}%`,
                            width: `${300 + Math.random() * 200}px`,
                            height: `${300 + Math.random() * 200}px`,
                            background: i % 2 === 0 ? 'rgba(56, 189, 248, 0.3)' : 'rgba(168, 85, 247, 0.3)', // Blue and Purple
                        }}
                    />
                ))}
            </div>

            {/* Animated Connection Lines (Abstract Network) */}
            <svg className="absolute inset-0 w-full h-full opacity-20 pointer-events-none">
                <motion.path
                    d="M0,50 Q400,0 800,100 T1600,200"
                    fill="none"
                    stroke="url(#gradient1)"
                    strokeWidth="2"
                    initial={{ pathLength: 0, opacity: 0 }}
                    animate={{ pathLength: 1, opacity: 1 }}
                    transition={{ duration: 3, repeat: Infinity, ease: "linear", repeatType: "loop" }}
                />
                <defs>
                    <linearGradient id="gradient1" x1="0%" y1="0%" x2="100%" y2="0%">
                        <stop offset="0%" stopColor="#38bdf8" stopOpacity="0" />
                        <stop offset="50%" stopColor="#38bdf8" />
                        <stop offset="100%" stopColor="#a855f7" stopOpacity="0" />
                    </linearGradient>
                </defs>
            </svg>
        </div>
    )
}
