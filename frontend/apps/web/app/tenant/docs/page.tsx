"use client"

import Link from "next/link"
import {
  ArrowRight,
  BookOpenText,
  Boxes,
  CheckCircle2,
  Copy,
  MapPinHouse,
  PackageCheck,
  Route,
  ShieldCheck,
  Smartphone,
  XCircle,
} from "lucide-react"
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"

const baseUrl = "https://swifttrack-backend.ajayv.online"
const pypiUrl = "https://pypi.org/project/swifttrack/"

const workflow = [
  {
    id: "auth",
    title: "Authenticate",
    description: "Login with email/password or mobile OTP and store the returned access token.",
    icon: ShieldCheck,
  },
  {
    id: "address",
    title: "Create address",
    description: "Create a saved pickup address and optionally make it the default address for the tenant.",
    icon: MapPinHouse,
  },
  {
    id: "quote",
    title: "Get quote",
    description: "Pass `pickupAddressId` plus drop coordinates to create a quote session and compute price.",
    icon: Boxes,
  },
  {
    id: "order",
    title: "Create order",
    description: "Send `quoteSessionId`, `pickupAddressId`, and nested dropoff details to create a shipment.",
    icon: Route,
  },
  {
    id: "track",
    title: "Track order",
    description: "Use the order id to fetch timeline and live tracking state as the shipment progresses.",
    icon: PackageCheck,
  },
  {
    id: "cancel",
    title: "Cancel order",
    description: "Cancel only while the backend allows it: currently `CREATED` and `ASSIGNED`.",
    icon: XCircle,
  },
] as const

const apiSections = [
  {
    value: "sdk",
    label: "SDK",
    heading: "Python SDK",
    summary: "Install the official package, set the production base URL, and work through a single client.",
    endpoint: "PyPI package: swifttrack",
    notes: [
      "Install from the published package index, not from a local wheel.",
      "Use the production base URL in your client configuration for real tenant traffic.",
      "The SDK is best suited for server-side or trusted backend integrations.",
    ],
    request: `pip install swifttrack`,
    response: `from swifttrack import SwiftTrackClient

client = SwiftTrackClient(
    base_url="${baseUrl}",
)

login = client.login("ops@example.com", "strong-password")
print(login.access_token)

addresses = client.addresses.list_addresses()
print(f"Loaded {len(addresses)} saved addresses")`,
  },
  {
    value: "auth",
    label: "Auth",
    heading: "Authentication",
    summary: "SwiftTrack exposes two login flows through AuthService. Protected endpoints expect the custom `token` header.",
    endpoint: `${baseUrl}/authservice/api/users`,
    notes: [
      "Email login endpoint: `POST /v1/login/emailAndPassword`.",
      "Mobile OTP endpoint: `POST /v1/login/mobileNumAndOtp`.",
      "Current backend implementation uses the same mobile endpoint for OTP send and OTP verify.",
      "If `otp` is omitted, the service sends an OTP response. Current implementation simulates OTP and sets it to `123456`.",
    ],
    request: `curl -X POST "${baseUrl}/authservice/api/users/v1/login/emailAndPassword" \\
  -H "Content-Type: application/json" \\
  -d '{
    "email": "ops@example.com",
    "password": "strong-password"
  }'

curl -X POST "${baseUrl}/authservice/api/users/v1/login/mobileNumAndOtp" \\
  -H "Content-Type: application/json" \\
  -d '{
    "mobileNum": "9876543210"
  }'

curl -X POST "${baseUrl}/authservice/api/users/v1/login/mobileNumAndOtp" \\
  -H "Content-Type: application/json" \\
  -d '{
    "mobileNum": "9876543210",
    "otp": "123456"
  }'`,
    response: `{
  "tokenType": "Bearer Token",
  "accessToken": "eyJhbGciOiJIUzI1NiJ9..."
}`,
  },
  {
    value: "address",
    label: "Address",
    heading: "Saved Address Management",
    summary: "Address APIs live under OrderService and are tenant-scoped through the token header.",
    endpoint: `${baseUrl}/orderservice/api/order/addresses/v1`,
    notes: [
      "List addresses: `GET /addresses/v1`.",
      "Get default address: `GET /addresses/v1/default`.",
      "Create address: `POST /addresses/v1`.",
      "Update address: `PUT /addresses/v1/{addressId}`.",
      "Set default address: `POST /addresses/v1/{addressId}/default`.",
      "Protected calls must include `token: <accessToken>`.",
    ],
    request: `curl -X POST "${baseUrl}/orderservice/api/order/addresses/v1" \\
  -H "Content-Type: application/json" \\
  -H "token: <access-token>" \\
  -d '{
    "label": "Warehouse",
    "line1": "12 Residency Road",
    "line2": "Dock 3",
    "city": "Bengaluru",
    "state": "Karnataka",
    "country": "India",
    "pincode": "560025",
    "locality": "Ashok Nagar",
    "latitude": 12.9716,
    "longitude": 77.5946,
    "contactName": "Inbound Ops",
    "contactPhone": "9876543210",
    "businessName": "SwiftTrack Hub",
    "notes": "Use loading bay entrance",
    "isDefault": true
  }'`,
    response: `{
  "id": "68c9395e-2a0a-4b7f-b8cb-7a6cbffdd111",
  "label": "Warehouse",
  "line1": "12 Residency Road",
  "city": "Bengaluru",
  "state": "Karnataka",
  "country": "India",
  "pincode": "560025",
  "latitude": 12.9716,
  "longitude": 77.5946,
  "contactName": "Inbound Ops",
  "contactPhone": "9876543210",
  "isDefault": true
}`,
  },
  {
    value: "quote",
    label: "Quote",
    heading: "Quote Creation",
    summary: "The tenant quote API uses a saved pickup address and raw drop coordinates. It returns a quote session id plus nested quote data.",
    endpoint: `${baseUrl}/orderservice/api/order/v1/getQuote`,
    notes: [
      "Method: `POST /api/order/v1/getQuote`.",
      "Headers: `token` plus `Content-Type: application/json`.",
      "Body shape is exactly `{ pickupAddressId, dropoffLat, dropoffLng }`.",
      "The price is nested under `quoteResponse.price`, not at the top level.",
    ],
    request: `curl -X POST "${baseUrl}/orderservice/api/order/v1/getQuote" \\
  -H "Content-Type: application/json" \\
  -H "token: <access-token>" \\
  -d '{
    "pickupAddressId": "68c9395e-2a0a-4b7f-b8cb-7a6cbffdd111",
    "dropoffLat": 12.9352,
    "dropoffLng": 77.6245
  }'`,
    response: `{
  "quoteResponse": {
    "price": 164.0,
    "currency": "INR",
    "quoteId": null
  },
  "quoteSessionId": "4ef0bd2f-4fc0-4bb8-9b86-93241f5c1111",
  "selectedType": "LOCAL_DRIVERS",
  "providerCode": null,
  "quoteId": null
}`,
  },
  {
    value: "order",
    label: "Create Order",
    heading: "Order Creation",
    summary: "Create order expects `quoteSessionId` as a query param and a nested `dropoff` payload in the request body.",
    endpoint: `${baseUrl}/orderservice/api/order/v1/createOrder`,
    notes: [
      "Method: `POST /api/order/v1/createOrder?quoteSessionId=<uuid>`.",
      "Pickup is referenced through `pickupAddressId`.",
      "Dropoff is a full `LocationPoint` object with nested `address` and `contact`.",
      "For tenant flow, `paymentType` should match backend enum values like `PREPAID` or `COD`.",
    ],
    request: `curl -X POST "${baseUrl}/orderservice/api/order/v1/createOrder?quoteSessionId=4ef0bd2f-4fc0-4bb8-9b86-93241f5c1111" \\
  -H "Content-Type: application/json" \\
  -H "token: <access-token>" \\
  -d '{
    "orderReference": "TENANT-ORD-1001",
    "orderType": "ON_DEMAND",
    "paymentType": "PREPAID",
    "pickupAddressId": "68c9395e-2a0a-4b7f-b8cb-7a6cbffdd111",
    "dropoff": {
      "addressId": null,
      "address": {
        "line1": "45 Indiranagar 100 Feet Road",
        "line2": null,
        "city": "Bengaluru",
        "state": "Karnataka",
        "country": "India",
        "pincode": "560038",
        "locality": "Indiranagar",
        "latitude": 12.9784,
        "longitude": 77.6408
      },
      "contact": {
        "name": "Customer Desk",
        "phone": "9988776655"
      },
      "businessName": "Destination Office",
      "notes": "Call on arrival",
      "verification": null
    },
    "packageInfo": {
      "totalValue": 2500,
      "totalWeightGrams": 3200,
      "size": "MEDIUM",
      "description": "Documents and device accessories"
    },
    "deliveryInstructions": "Deliver to reception"
  }'`,
    response: `{
  "orderId": "39aa55d3-9914-4e1f-8f8b-29e1afae2222",
  "providerCode": "SWIFTTRACK",
  "totalAmount": 164.0,
  "choiceCode": "SWIFTTRACK_DRIVER"
}`,
  },
  {
    value: "track",
    label: "Track",
    heading: "Tracking and Order State",
    summary: "Order state and tracking timeline are exposed separately so clients can choose lightweight or full-detail polling.",
    endpoint: `${baseUrl}/orderservice/api/order`,
    notes: [
      "Tracking timeline: `GET /v1/getOrderTracking/{orderId}`.",
      "Order status only: `GET /v1/getOrderStatus/{orderId}`.",
      "Full order details: `GET /v1/getOrderById/{orderId}`.",
      "All protected endpoints require the `token` header.",
    ],
    request: `curl -X GET "${baseUrl}/orderservice/api/order/v1/getOrderTracking/39aa55d3-9914-4e1f-8f8b-29e1afae2222" \\
  -H "token: <access-token>"`,
    response: `{
  "orderId": "39aa55d3-9914-4e1f-8f8b-29e1afae2222",
  "orderStatus": "ASSIGNED",
  "trackingStatus": "ASSIGNED",
  "lastStatusUpdatedAt": "2026-03-30T10:22:14",
  "lastLocationUpdatedAt": null,
  "currentLocation": null,
  "events": []
}`,
  },
  {
    value: "cancel",
    label: "Cancel",
    heading: "Order Cancellation",
    summary: "Cancellation is a protected write operation and the backend currently allows it only in `CREATED` or `ASSIGNED`.",
    endpoint: `${baseUrl}/orderservice/api/order/v1/cancelOrder`,
    notes: [
      "Method: `POST /api/order/v1/cancelOrder?orderId=<uuid>`.",
      "Current backend validation rejects cancellation for `PICKED_UP`, `IN_TRANSIT`, `DELIVERED`, `FAILED`, and `CANCELLED`.",
      "For external providers the service also forwards a provider-level cancellation call before updating SwiftTrack state.",
    ],
    request: `curl -X POST "${baseUrl}/orderservice/api/order/v1/cancelOrder?orderId=39aa55d3-9914-4e1f-8f8b-29e1afae2222" \\
  -H "token: <access-token>"`,
    response: `{
  "message": "Order cancelled successfully"
}`,
  },
] as const

const sdkWorkflow = `from swifttrack import SwiftTrackClient
from swifttrack.models.address import AddressRequest

client = SwiftTrackClient(base_url="${baseUrl}")

# 1. Login
login = client.login("ops@example.com", "strong-password")
token = login.access_token

# 2. Create pickup address
pickup = client.addresses.create_address(
    AddressRequest(
        label="Warehouse",
        line1="12 Residency Road",
        city="Bengaluru",
        state="Karnataka",
        country="India",
        pincode="560025",
        latitude=12.9716,
        longitude=77.5946,
        contact_name="Inbound Ops",
        contact_phone="9876543210",
        is_default=True,
    )
)

# 3. Get quote
quote = client.orders.get_quote(
    pickup_address_id=pickup.id,
    dropoff_lat=12.9352,
    dropoff_lng=77.6245,
)

# 4. Create order
# Pass quote.quote_session_id into the order creation call

# 5. Track or cancel later using the returned order id`

export default function TenantDocsPage() {
  return (
    <div className="min-h-full bg-[radial-gradient(circle_at_top_left,_rgba(15,23,42,0.06),_transparent_26%),linear-gradient(180deg,#f8fafc,#eef2ff)]">
      <div className="mx-auto flex w-full max-w-7xl flex-col gap-6 px-4 py-6 sm:px-6 lg:px-8">
        <section className="overflow-hidden rounded-[30px] border border-slate-200 bg-[linear-gradient(135deg,#fff8eb,#ffffff_45%,#eef4ff)] shadow-sm">
          <div className="grid gap-8 px-6 py-8 lg:grid-cols-[1.15fr_0.85fr] lg:px-8">
            <div className="space-y-5">
              <Badge variant="outline" className="rounded-full border-amber-200 bg-white/80 px-3 py-1 text-amber-700">
                Developer Docs
              </Badge>
              <div className="space-y-3">
                <h1 className="max-w-3xl text-3xl font-semibold tracking-tight text-slate-950 md:text-4xl">
                  SDK and API documentation for SwiftTrack tenant integrations
                </h1>
                <p className="max-w-2xl text-sm leading-7 text-slate-600">
                  Production-ready integration guide for the SwiftTrack Python SDK and the tenant order flow. This page
                  uses the live backend base URL, real controller payloads, and the exact sequence developers need:
                  auth, address, quote, order, tracking, and cancellation.
                </p>
              </div>
              <div className="flex flex-wrap gap-3">
                <Button asChild className="rounded-full bg-slate-950 text-white hover:bg-slate-800">
                  <a href={pypiUrl} target="_blank" rel="noreferrer">
                    Open PyPI
                  </a>
                </Button>
                <Button asChild variant="outline" className="rounded-full border-slate-300 bg-white">
                  <a href={baseUrl} target="_blank" rel="noreferrer">
                    Open Backend
                  </a>
                </Button>
                <Button asChild variant="outline" className="rounded-full border-slate-300 bg-white">
                  <Link href="/tenant/orders/create">Try Create Shipment</Link>
                </Button>
              </div>
            </div>

            <div className="grid gap-4 sm:grid-cols-2">
              <MetricCard title="Base URL" value={baseUrl} description="Use this instead of localhost for production docs and examples." />
              <MetricCard title="SDK" value="pip install swifttrack" description="Published package with typed models and structured clients." />
              <MetricCard title="Auth Header" value="token: <accessToken>" description="Protected tenant APIs use a custom token header, not Bearer auth in examples." />
              <MetricCard title="Order Flow" value="Auth -> Address -> Quote -> Order" description="Quote session id from step 3 is required for step 4." />
            </div>
          </div>
        </section>

        <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-6">
          {workflow.map((step, index) => (
            <Card key={step.id} className="border-slate-200 bg-white shadow-sm">
              <CardContent className="flex h-full flex-col gap-3 px-5 py-5">
                <div className="flex items-center justify-between">
                  <div className="rounded-2xl bg-slate-100 p-3 text-slate-700">
                    <step.icon className="h-5 w-5" />
                  </div>
                  <span className="text-xs font-medium uppercase tracking-[0.2em] text-slate-400">0{index + 1}</span>
                </div>
                <div className="space-y-1">
                  <p className="text-base font-semibold text-slate-950">{step.title}</p>
                  <p className="text-sm leading-6 text-slate-600">{step.description}</p>
                </div>
              </CardContent>
            </Card>
          ))}
        </section>

        <section className="grid gap-6 xl:grid-cols-[280px_minmax(0,1fr)]">
          <Card className="border-slate-200 bg-white shadow-sm xl:sticky xl:top-6 xl:self-start">
            <CardHeader className="border-b border-slate-100">
              <CardTitle className="text-slate-950">Docs Sections</CardTitle>
              <CardDescription>Jump between SDK usage and endpoint-by-endpoint API documentation.</CardDescription>
            </CardHeader>
            <CardContent className="space-y-3 pt-6">
              {apiSections.map((section) => (
                <a
                  key={section.value}
                  href={`#${section.value}`}
                  className="flex items-center justify-between rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-medium text-slate-700 transition hover:border-slate-300 hover:bg-white"
                >
                  <span>{section.heading}</span>
                  <ArrowRight className="h-4 w-4" />
                </a>
              ))}
            </CardContent>
          </Card>

          <div className="space-y-6">
            <Card className="border-slate-200 bg-white shadow-sm">
              <CardHeader className="border-b border-slate-100">
                <CardTitle className="text-slate-950">Interactive Reference</CardTitle>
                <CardDescription>
                  Each section includes endpoint mapping, payload shape, implementation notes, and copy-friendly examples.
                </CardDescription>
              </CardHeader>
              <CardContent className="pt-6">
                <Tabs defaultValue="sdk" className="gap-6">
                  <TabsList variant="line" className="flex w-full flex-wrap justify-start gap-2 rounded-2xl bg-slate-50 p-2">
                    {apiSections.map((section) => (
                      <TabsTrigger
                        key={section.value}
                        value={section.value}
                        className="rounded-full border border-transparent px-4 py-2 data-[state=active]:border-slate-200 data-[state=active]:bg-white"
                      >
                        {section.label}
                      </TabsTrigger>
                    ))}
                  </TabsList>

                  {apiSections.map((section) => (
                    <TabsContent key={section.value} value={section.value} id={section.value} className="space-y-6">
                      <div className="space-y-2">
                        <p className="text-xs font-medium uppercase tracking-[0.2em] text-slate-500">{section.label}</p>
                        <h2 className="text-2xl font-semibold tracking-tight text-slate-950">{section.heading}</h2>
                        <p className="max-w-3xl text-sm leading-7 text-slate-600">{section.summary}</p>
                      </div>

                      <div className="grid gap-4 lg:grid-cols-[1.1fr_0.9fr]">
                        <InfoCard title="Endpoint / Resource" value={section.endpoint} />
                        <InfoCard title="Integration Focus" value={section.label === "SDK" ? "Python client integration" : "Raw HTTP + tenant token"} />
                      </div>

                      <div className="grid gap-4 2xl:grid-cols-[0.95fr_1.05fr]">
                        <Card className="border-slate-200 bg-slate-50 shadow-none">
                          <CardHeader>
                            <CardTitle className="text-base text-slate-950">Implementation Notes</CardTitle>
                          </CardHeader>
                          <CardContent className="space-y-3 text-sm leading-6 text-slate-600">
                            {section.notes.map((note) => (
                              <div key={note} className="rounded-2xl border border-slate-200 bg-white px-4 py-3">
                                {note}
                              </div>
                            ))}
                          </CardContent>
                        </Card>

                        <div className="grid gap-4">
                          <CodePanel title="Example Request" code={section.request} />
                          <CodePanel title="Example Response" code={section.response} />
                        </div>
                      </div>
                    </TabsContent>
                  ))}
                </Tabs>
              </CardContent>
            </Card>

            <Card className="border-slate-200 bg-white shadow-sm" id="sdk">
              <CardHeader className="border-b border-slate-100">
                <CardTitle className="text-slate-950">SDK Quickstart</CardTitle>
                <CardDescription>Use the published Python client when you want typed models and a cleaner integration layer.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-6 pt-6">
                <div className="grid gap-4 md:grid-cols-3">
                  <ChecklistCard title="Install" detail="pip install swifttrack" />
                  <ChecklistCard title="Configure" detail={`base_url="${baseUrl}"`} />
                  <ChecklistCard title="Authenticate" detail="client.login(...) returns accessToken" />
                </div>
                <CodePanel title="SDK Starter Flow" code={sdkWorkflow} />
              </CardContent>
            </Card>

            <Card className="border-slate-200 bg-white shadow-sm">
              <CardHeader className="border-b border-slate-100">
                <CardTitle className="text-slate-950">Integration FAQ</CardTitle>
                <CardDescription>Important backend behaviors that will save implementation time.</CardDescription>
              </CardHeader>
              <CardContent className="pt-6">
                <Accordion type="single" collapsible className="w-full">
                  <AccordionItem value="token">
                    <AccordionTrigger>Should I use Authorization bearer headers?</AccordionTrigger>
                    <AccordionContent className="text-slate-600">
                      The tenant-facing services in this codebase expect the custom `token` header on protected requests.
                      The docs on this page intentionally use that header format because it matches the working frontend
                      and controller integrations.
                    </AccordionContent>
                  </AccordionItem>
                  <AccordionItem value="quote-shape">
                    <AccordionTrigger>Why is quote price nested?</AccordionTrigger>
                    <AccordionContent className="text-slate-600">
                      `POST /api/order/v1/getQuote` returns a top-level object with `quoteSessionId`, `selectedType`,
                      `providerCode`, and a nested `quoteResponse`. Read `quoteResponse.price` and `quoteResponse.currency`
                      when rendering the quote.
                    </AccordionContent>
                  </AccordionItem>
                  <AccordionItem value="otp">
                    <AccordionTrigger>How does mobile OTP login work right now?</AccordionTrigger>
                    <AccordionContent className="text-slate-600">
                      The same mobile login endpoint is used for OTP send and OTP verification. If `otp` is omitted, the
                      backend sends an OTP response. The current implementation simulates the OTP and sets it to `123456`.
                    </AccordionContent>
                  </AccordionItem>
                  <AccordionItem value="cancel">
                    <AccordionTrigger>Which orders can be cancelled?</AccordionTrigger>
                    <AccordionContent className="text-slate-600">
                      Current backend validation allows cancellation only when the order status is `CREATED` or
                      `ASSIGNED`. Anything already picked up, in transit, delivered, failed, or already cancelled is
                      rejected by the service.
                    </AccordionContent>
                  </AccordionItem>
                </Accordion>
              </CardContent>
            </Card>

            <Card className="border-slate-200 bg-[linear-gradient(135deg,#0f172a,#111827)] text-white shadow-sm">
              <CardContent className="flex flex-col gap-5 px-6 py-6 lg:flex-row lg:items-center lg:justify-between">
                <div className="space-y-2">
                  <p className="text-xs font-medium uppercase tracking-[0.22em] text-sky-300">Next Step</p>
                  <h3 className="text-2xl font-semibold tracking-tight">Build against the same flow used by the tenant dashboard</h3>
                  <p className="max-w-2xl text-sm leading-6 text-slate-300">
                    Start with saved address creation, move to quote generation, and keep `quoteSessionId` as the join
                    point between pricing and order creation.
                  </p>
                </div>
                <div className="flex flex-wrap gap-3">
                  <Button asChild className="rounded-full bg-white text-slate-950 hover:bg-slate-100">
                    <Link href="/tenant/addresses">Manage Addresses</Link>
                  </Button>
                  <Button asChild variant="outline" className="rounded-full border-white/20 bg-white/5 text-white hover:bg-white/10">
                    <Link href="/tenant/orders/create">Create Shipment</Link>
                  </Button>
                </div>
              </CardContent>
            </Card>
          </div>
        </section>
      </div>
    </div>
  )
}

function MetricCard({ title, value, description }: { title: string; value: string; description: string }) {
  return (
    <Card className="border-slate-200 bg-white/90 shadow-sm">
      <CardContent className="space-y-2 px-5 py-5">
        <p className="text-xs font-medium uppercase tracking-[0.2em] text-slate-500">{title}</p>
        <p className="break-words text-sm font-semibold text-slate-950">{value}</p>
        <p className="text-sm leading-6 text-slate-600">{description}</p>
      </CardContent>
    </Card>
  )
}

function InfoCard({ title, value }: { title: string; value: string }) {
  return (
    <Card className="border-slate-200 bg-slate-50 shadow-none">
      <CardContent className="space-y-2 px-5 py-5">
        <p className="text-xs font-medium uppercase tracking-[0.2em] text-slate-500">{title}</p>
        <p className="break-words text-sm font-semibold text-slate-950">{value}</p>
      </CardContent>
    </Card>
  )
}

function CodePanel({ title, code }: { title: string; code: string }) {
  return (
    <Card className="overflow-hidden border-slate-200 bg-slate-950 text-slate-100 shadow-none">
      <CardHeader className="border-b border-white/10 pb-4">
        <div className="flex items-center justify-between gap-3">
          <CardTitle className="text-base text-white">{title}</CardTitle>
          <div className="rounded-full border border-white/10 bg-white/5 p-2 text-slate-300">
            <Copy className="h-4 w-4" />
          </div>
        </div>
      </CardHeader>
      <CardContent className="p-0">
        <div className="max-w-full overflow-x-auto">
          <pre className="min-w-full px-4 py-5 text-sm leading-6 text-slate-200 sm:px-5">
            <code className="block whitespace-pre">{code}</code>
          </pre>
        </div>
      </CardContent>
    </Card>
  )
}

function ChecklistCard({ title, detail }: { title: string; detail: string }) {
  return (
    <Card className="border-slate-200 bg-slate-50 shadow-none">
      <CardContent className="flex items-start gap-3 px-5 py-5">
        <div className="rounded-2xl bg-emerald-100 p-2 text-emerald-700">
          <CheckCircle2 className="h-4 w-4" />
        </div>
        <div className="min-w-0 space-y-1">
          <p className="text-sm font-semibold text-slate-950">{title}</p>
          <p className="break-words text-sm leading-6 text-slate-600">{detail}</p>
        </div>
      </CardContent>
    </Card>
  )
}
