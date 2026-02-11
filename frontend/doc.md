# SwiftTrack Frontend Monorepo Architecture

This document defines the recommended modern frontend architecture for **SwiftTrack**, designed to support both **web** and **mobile** applications while maximizing code reuse, scalability, and developer productivity.

The system is built using a **monorepo** structure so that UI components, business logic, types, and API layers can be shared across platforms.

---

## 🧠 Monorepo Overview

A single repository contains multiple applications and shared libraries.

```
swifttrack/
 ├─ apps/
 │   ├─ web/        → Next.js web application
 │   └─ mobile/     → Expo (React Native) mobile application
 │
 ├─ libs/
 │   ├─ ui/         → Shared UI components
 │   ├─ types/      → Shared TypeScript types
 │   ├─ api/        → API client layer
 │   ├─ utils/      → Helpers, hooks, constants
 │   └─ config/     → Shared ESLint, TS, Tailwind configs
```

---

## 🌍 Web Application Stack

| Layer            | Technology            |
| ---------------- | --------------------- |
| Framework        | Next.js (App Router)  |
| Language         | TypeScript            |
| Styling          | Tailwind CSS          |
| UI Components    | Shared from `libs/ui` |
| Data Fetching    | TanStack Query        |
| Forms            | React Hook Form + Zod |
| State Management | Zustand               |
| Authentication   | Auth.js or Clerk      |

**Why this works**

* Server-side rendering for dashboards and SEO
* Shared API and types with mobile
* Fast builds via monorepo caching

---

## 📱 Mobile Application Stack

| Layer            | Technology                             |
| ---------------- | -------------------------------------- |
| Framework        | Expo + React Native                    |
| Language         | TypeScript                             |
| Styling          | NativeWind (Tailwind for React Native) |
| Navigation       | Expo Router                            |
| Data Fetching    | TanStack Query                         |
| State Management | Zustand                                |

**Why Expo**

* Fast development cycle
* Over-the-air updates
* Easy expansion with native modules

---

## 🎨 Shared UI Strategy

### Option A — Cross-Platform UI (Recommended)

Build UI components using React Native primitives inside `libs/ui`.
These components run on:

* Mobile via React Native
* Web via react-native-web

Best suited for:

* Buttons
* Cards
* List items
* Inputs
* Design system components

### Option B — Platform-Specific UI

If designs differ significantly between platforms:

```
libs/ui/button/
  button.web.tsx
  button.native.tsx
```

The bundler automatically picks the correct file per platform.

---

## 🔌 Shared API Layer

All backend communication is centralized in:

```
libs/api/
```

| Concern      | Tooling                       |
| ------------ | ----------------------------- |
| API Calls    | tRPC or REST (Axios/Fetch)    |
| Validation   | Zod                           |
| Shared Types | Generated from backend schema |

Both apps import API logic like:

```ts
import { getTrackingStatus } from '@swifttrack/api'
```

This prevents duplication and ensures consistent data handling.

---

## 🧩 Shared Logic Libraries

| Library      | Purpose                             |
| ------------ | ----------------------------------- |
| `libs/types` | Order, shipment, and user types     |
| `libs/utils` | Date formatting, constants, helpers |
| `libs/hooks` | Shared hooks (auth, tracking, etc.) |

---

## ⚡ Developer Tooling

| Tool              | Purpose                             |
| ----------------- | ----------------------------------- |
| Monorepo Tooling  | Task orchestration and caching      |
| Nx Cloud          | Distributed caching and CI speedups |
| Storybook         | Isolated UI component development   |
| ESLint + Prettier | Code quality and formatting         |
| Jest / Vitest     | Unit and integration testing        |
| Husky             | Git hooks for quality checks        |

---

## 🚀 Project Setup Commands

```bash
npx create-nx-workspace@latest swifttrack
cd swifttrack

# Add Next.js web app
nx g @nx/next:app web

# Add Expo mobile app
nx g @nx/expo:app mobile

# Create shared libraries
nx g @nx/js:lib ui
nx g @nx/js:lib api
nx g @nx/js:lib types
nx g @nx/js:lib utils
```

---

## 🏆 Why This Architecture Is Future‑Proof

* One team can build web and mobile together
* Shared design system ensures consistency
* Shared API and types reduce bugs
* Faster CI/CD with intelligent caching
* Easy to add additional apps (admin, driver app, etc.)

---

## 🔥 Final Stack Summary

| Layer         | Technology                       |
| ------------- | -------------------------------- |
| Monorepo      | Nx                               |
| Web           | Next.js + Tailwind               |
| Mobile        | Expo + React Native + NativeWind |
| Shared UI     | React Native + react-native-web  |
| State         | Zustand                          |
| API           | tRPC or REST + Zod               |
| Data Fetching | TanStack Query                   |
| Auth          | Auth.js or Clerk                 |

---

This setup enables SwiftTrack to scale across platforms while keeping development fast, maintainable, and consistent.
