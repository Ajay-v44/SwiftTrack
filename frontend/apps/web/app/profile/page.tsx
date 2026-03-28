"use client"

import * as React from "react"
import { useAuthStore } from "@/store/useAuthStore"
import { useRouter } from "next/navigation"
import { motion } from "framer-motion"
import { User, Phone, Briefcase, Shield, Building, CreditCard, LogOut, Loader2, ArrowLeft } from "lucide-react"

import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Badge } from "@/components/ui/badge"
import { Separator } from "@/components/ui/separator"
import Link from "next/link"

export default function ProfilePage() {
  const { user, isLoading, logout } = useAuthStore()
  const router = useRouter()

  React.useEffect(() => {
    if (!isLoading && !user) {
      router.push("/login")
    }
  }, [user, isLoading, router])

  if (isLoading || !user) {
    return (
      <div className="flex min-h-screen items-center justify-center p-4">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    )
  }

  const getInitials = (name: string) => {
    return name?.split(" ").map(n => n[0]).join("").toUpperCase() || "U"
  }

  const handleLogout = () => {
    logout()
    router.push("/login")
  }

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 p-4 pt-24 pb-12 relative overflow-hidden">
      {/* Decorative Blobs */}
      <div className="absolute top-1/4 -right-20 w-72 h-72 bg-primary/10 rounded-full mix-blend-multiply filter blur-3xl opacity-70 animate-blob" />
      <div className="absolute bottom-1/4 -left-20 w-96 h-96 bg-purple-500/10 rounded-full mix-blend-multiply filter blur-3xl opacity-70 animate-blob animation-delay-2000" />
      
      <div className="max-w-4xl mx-auto space-y-8 z-10 relative">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={() => router.back()} className="rounded-full">
            <ArrowLeft className="h-5 w-5" />
          </Button>
          <div>
            <h1 className="text-3xl font-bold tracking-tight">My Profile</h1>
            <p className="text-muted-foreground">Manage your account settings and preferences.</p>
          </div>
        </div>

        <div className="grid gap-8 md:grid-cols-3">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.4 }}
            className="md:col-span-1 space-y-6"
          >
            <Card className="glass-card shadow-lg border-primary/10">
              <CardContent className="p-6 flex flex-col items-center text-center">
                <Avatar className="h-24 w-24 mb-4 border-4 border-background shadow-xl">
                  <AvatarImage src={`https://api.dicebear.com/7.x/initials/svg?seed=${user.name}`} />
                  <AvatarFallback className="text-2xl">{getInitials(user.name)}</AvatarFallback>
                </Avatar>
                <h2 className="text-xl font-bold">{user.name}</h2>
                <Badge variant="secondary" className="mt-2 capitalize bg-primary/10 text-primary hover:bg-primary/20">
                  {(user.type || "ACCOUNT").replace(/_/g, " ").toLowerCase()}
                </Badge>
                
                <div className="mt-6 w-full flex flex-col gap-2">
                  <div className="flex items-center gap-3 text-sm text-muted-foreground bg-muted/50 p-3 rounded-lg border border-border/50">
                    <Phone className="h-4 w-4 text-primary" />
                    <span>{user.mobile || "No mobile number provided"}</span>
                  </div>
                  {user.roles && user.roles.length > 0 && (
                    <div className="flex items-center gap-3 text-sm text-muted-foreground bg-muted/50 p-3 rounded-lg border border-border/50">
                      <Shield className="h-4 w-4 text-primary" />
                      <div className="flex flex-wrap gap-1">
                        {user.roles.map(role => (
                          <span key={role} className="lowercase first-letter:capitalize">{role.replace(/_/g, " ")}</span>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              </CardContent>
              <CardFooter className="p-4 pt-0">
                <Button variant="destructive" className="w-full gap-2" onClick={handleLogout}>
                  <LogOut className="h-4 w-4" />
                  Sign Out
                </Button>
              </CardFooter>
            </Card>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.4, delay: 0.1 }}
            className="md:col-span-2 space-y-6"
          >
            <Card className="glass-card shadow-lg border-primary/10">
              <CardHeader>
                <CardTitle className="text-xl flex items-center gap-2">
                  <User className="h-5 w-5 text-primary" />
                  Personal Information
                </CardTitle>
                <CardDescription>
                  Update your personal details here.
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="grid gap-4 sm:grid-cols-2">
                  <div className="space-y-2">
                    <Label htmlFor="name">Full Name</Label>
                    <Input id="name" defaultValue={user.name} readOnly className="bg-muted/30" />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="mobile">Mobile Number</Label>
                    <Input id="mobile" defaultValue={user.mobile} readOnly className="bg-muted/30" />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="id">User ID</Label>
                  <Input id="id" defaultValue={user.id} readOnly className="font-mono text-xs bg-muted/30" />
                  <p className="text-xs text-muted-foreground">Unique identifier linked to your account.</p>
                </div>
              </CardContent>
            </Card>

            {(user.tenantId || user.providerId) && (
              <Card className="glass-card shadow-lg border-primary/10">
                <CardHeader>
                  <CardTitle className="text-xl flex items-center gap-2">
                    <Building className="h-5 w-5 text-primary" />
                    Organization Details
                  </CardTitle>
                  <CardDescription>
                    Information regarding your associated organization or provider.
                  </CardDescription>
                </CardHeader>
                <CardContent className="space-y-6">
                  {user.tenantId && (
                    <div className="space-y-2">
                      <Label htmlFor="tenantId">Tenant ID</Label>
                      <Input id="tenantId" defaultValue={user.tenantId} readOnly className="font-mono text-xs bg-muted/30" />
                    </div>
                  )}
                  {user.providerId && (
                    <div className="space-y-2">
                      <Label htmlFor="providerId">Provider ID</Label>
                      <Input id="providerId" defaultValue={user.providerId} readOnly className="font-mono text-xs bg-muted/30" />
                    </div>
                  )}
                </CardContent>
              </Card>
            )}

            <Card className="border-dashed border-2 bg-muted/10">
              <CardHeader>
                <CardTitle className="text-lg">Account Actions</CardTitle>
              </CardHeader>
              <CardContent className="flex flex-col sm:flex-row gap-4">
                 <Button variant="outline" className="w-full sm:w-auto" disabled>
                   Reset Password
                 </Button>
                 <Button variant="outline" className="w-full sm:w-auto text-destructive border-destructive/20 hover:bg-destructive/10 hover:text-destructive" disabled>
                   Request Account Deletion
                 </Button>
              </CardContent>
            </Card>
          </motion.div>
        </div>
      </div>
    </div>
  )
}
