"use client"

import * as React from "react"
import { useState } from "react"
import { motion, AnimatePresence } from "framer-motion"
import { Mail, Phone, Lock, ArrowRight, Loader2, CheckCircle2 } from "lucide-react"
import { toast } from "sonner"

import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs"
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { useLogin } from "@/hooks/useLogin"

export default function LoginPage() {
  const { isLoading, otpSent, loginWithEmail, loginWithPhone } = useLogin()
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [mobile, setMobile] = useState("")
  const [otp, setOtp] = useState("")
  const [activeTab, setActiveTab] = useState("email")

  const handleEmailLogin = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!email || !password) {
      toast.error("Please fill in all fields")
      return
    }

    try {
      await loginWithEmail(email, password)
    } catch {
      return
    }
  }

  const handlePhoneLogin = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!mobile) {
      toast.error("Please enter your mobile number")
      return
    }

    if (otpSent && !otp) {
      toast.error("Please enter the OTP")
      return
    }

    try {
      await loginWithPhone(mobile, otp)
    } catch {
      return
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 dark:bg-slate-950 p-4 relative overflow-hidden">
      <div className="absolute top-1/4 -left-20 w-72 h-72 bg-primary/20 rounded-full mix-blend-multiply filter blur-3xl opacity-70 animate-blob" />
      <div className="absolute bottom-1/4 -right-20 w-96 h-96 bg-purple-500/20 rounded-full mix-blend-multiply filter blur-3xl opacity-70 animate-blob animation-delay-2000" />

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
        className="w-full max-w-md z-10"
      >
        <div className="text-center mb-10">
          <motion.h1
            className="text-4xl font-bold gradient-text tracking-tight mb-2"
            initial={{ scale: 0.9 }}
            animate={{ scale: 1 }}
          >
            SwiftTrack
          </motion.h1>
          <p className="text-muted-foreground">Premium Fleet &amp; Order Management</p>
        </div>

        <Card className="glass-card">
          <CardHeader>
            <CardTitle className="text-2xl">Welcome Back</CardTitle>
            <CardDescription>
              Choose your preferred login method to continue
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
              <TabsList className="grid w-full grid-cols-2 mb-8 bg-muted/50 p-1">
                <TabsTrigger value="email" className="flex items-center gap-2">
                  <Mail className="h-4 w-4" />
                  Email
                </TabsTrigger>
                <TabsTrigger value="phone" className="flex items-center gap-2">
                  <Phone className="h-4 w-4" />
                  Phone
                </TabsTrigger>
              </TabsList>

              <AnimatePresence mode="wait">
                <motion.div
                  key={activeTab}
                  initial={{ opacity: 0, x: activeTab === "email" ? -20 : 20 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0, x: activeTab === "email" ? 20 : -20 }}
                  transition={{ duration: 0.2 }}
                >
                  <TabsContent value={activeTab} forceMount className="mt-0">
                    {activeTab === "email" ? (
                      <form onSubmit={handleEmailLogin} className="space-y-4">
                        <div className="space-y-2">
                          <Label htmlFor="email">Email</Label>
                          <div className="relative">
                            <Mail className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
                            <Input
                              id="email"
                              type="email"
                              placeholder="name@company.com"
                              className="pl-10"
                              value={email}
                              onChange={(event) => setEmail(event.target.value)}
                              required
                            />
                          </div>
                        </div>
                        <div className="space-y-2">
                          <div className="flex items-center justify-between">
                            <Label htmlFor="password">Password</Label>
                            <Button variant="link" size="sm" className="px-0 text-xs text-primary">
                              Forgot Password?
                            </Button>
                          </div>
                          <div className="relative">
                            <Lock className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
                            <Input
                              id="password"
                              type="password"
                              className="pl-10"
                              value={password}
                              onChange={(event) => setPassword(event.target.value)}
                              required
                            />
                          </div>
                        </div>
                        <Button
                          className="w-full mt-2 h-11 bg-primary hover:bg-primary/90 transition-all font-medium"
                          disabled={isLoading}
                        >
                          {isLoading ? (
                            <>
                              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                              Authenticating...
                            </>
                          ) : (
                            <>
                              Sign In
                              <ArrowRight className="ml-2 h-4 w-4" />
                            </>
                          )}
                        </Button>
                      </form>
                    ) : (
                      <form onSubmit={handlePhoneLogin} className="space-y-4">
                        {!otpSent ? (
                          <div className="space-y-2">
                            <Label htmlFor="mobile">Phone Number</Label>
                            <div className="relative">
                              <Phone className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
                              <Input
                                id="mobile"
                                type="tel"
                                placeholder="+1 (555) 000-0000"
                                className="pl-10"
                                value={mobile}
                                onChange={(event) => setMobile(event.target.value)}
                                required
                              />
                            </div>
                            <p className="text-xs text-muted-foreground">
                              We&apos;ll send a 6-digit one-time password to your phone.
                            </p>
                          </div>
                        ) : (
                          <div className="space-y-4">
                            <div className="bg-primary/5 p-4 rounded-lg flex items-center gap-3 border border-primary/10">
                              <CheckCircle2 className="h-5 w-5 text-primary" />
                              <div>
                                <p className="text-sm font-medium">OTP Sent</p>
                                <p className="text-xs text-muted-foreground">Sent to {mobile}</p>
                              </div>
                            </div>
                            <div className="space-y-2">
                              <Label htmlFor="otp">Verification Code</Label>
                              <Input
                                id="otp"
                                placeholder="Enter 6-digit code"
                                className="text-center tracking-widest text-lg h-12"
                                value={otp}
                                maxLength={6}
                                onChange={(event) => setOtp(event.target.value.replace(/\D/g, ""))}
                                required
                              />
                            </div>
                          </div>
                        )}

                        <Button
                          className="w-full mt-2 h-11 bg-primary hover:bg-primary/90 transition-all font-medium"
                          disabled={isLoading}
                        >
                          {isLoading ? (
                            <>
                              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                              {otpSent ? "Verifying..." : "Sending OTP..."}
                            </>
                          ) : (
                            <>
                              {otpSent ? "Verify & Sign In" : "Send One-Time Password"}
                              <ArrowRight className="ml-2 h-4 w-4" />
                            </>
                          )}
                        </Button>
                      </form>
                    )}
                  </TabsContent>
                </motion.div>
              </AnimatePresence>
            </Tabs>
          </CardContent>
          <CardFooter className="flex flex-col gap-4">
            <div className="relative w-full">
              <div className="absolute inset-0 flex items-center">
                <span className="w-full border-t" />
              </div>
              <div className="relative flex justify-center text-xs uppercase">
                <span className="bg-card px-2 text-muted-foreground">Or</span>
              </div>
            </div>
            <p className="text-center text-sm text-balance text-muted-foreground">
              By clicking continue, you agree to our{" "}
              <Button variant="link" className="h-auto p-0 text-primary">Terms of Service</Button> and{" "}
              <Button variant="link" className="h-auto p-0 text-primary">Privacy Policy</Button>.
            </p>
          </CardFooter>
        </Card>
      </motion.div>
    </div>
  )
}
