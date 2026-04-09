# SwiftTrack Frontend Monorepo

Welcome to the **SwiftTrack** frontend workspace. This project is built as a **Monorepo** using [Nx](https://nx.dev), allowing us to manage multiple applications (Web, Mobile) and shared libraries in a single codebase.

## 📂 Canvas Structure

The workspace is organized as follows:

- **apps/**: Contains the main application entry points.
  - **web/**: The Next.js dashboard and landing page.
  - **mobileapp/**: The React Native (Expo) application for drivers/users.
- **libs/**: Contains shared code used across applications.
  - **shared-ui/**: Reusable UI components, hooks, and utilities.
- **tools/**: Custom scripts and workspace configurations.

---

## 🚀 Setup & Installation

Since this is a monorepo, you only need to install dependencies once at the root level:

```bash
# In the root frontend directory:
npm install
```

## 🚀 Running Tasks

We use **Nx** to run tasks efficiently. While you can run specific commands inside folders, using `nx` commands from the root provides caching and better orchestration.

### Common Commands

| Task | Command | Description |
|------|---------|-------------|
| **Start Web App** | `npx nx dev web` | Starts the Next.js web application locally. |
| **Start Mobile App** | `npx nx start mobileapp` | Starts the Expo React Native app. Alternatively: `npx expo start --prefix apps/mobileapp` |
| **Build Web** | `npx nx build web` | Builds the web application for production. |
| **Lint** | `npx nx lint web` | Runs ESLint checks for the web app. |
| **Test** | `npx nx test web` | Runs unit tests for the web app. |
| **Build Native App** | `cd apps/mobileapp && npx eas build --profile development --platform android` | Triggers a fresh cloud build for the React Native App via Expo EAS. |
| **Publish OTA Update** | `cd apps/mobileapp && npx eas update --branch production` | Deploys Over-The-Air updates directly to users without app stores. |

> **Note:** Always run commands (like `npx nx dev web` or `npx nx start mobileapp`) from the root `frontend` folder!

### EAS Android Firebase Setup

Android EAS builds require `google-services.json`, but this repository intentionally ignores that file so secrets are not committed.

For local development inside `apps/mobileapp`, keep your file at:

```bash
apps/mobileapp/google-services.json
```

For EAS cloud builds, upload the same file as an EAS file secret and expose it as `GOOGLE_SERVICES_JSON`:

```bash
cd apps/mobileapp
npx eas secret:create --scope project --type file --name GOOGLE_SERVICES_JSON --value ./google-services.json
```

The Expo app config resolves Android Firebase in this order:
1. `GOOGLE_SERVICES_JSON` from EAS
2. Local `apps/mobileapp/google-services.json`

Because `apps/mobileapp/android` is committed, EAS treats the mobile app as a native project. During `eas-build-post-install`, we copy the resolved Firebase file into `apps/mobileapp/android/app/google-services.json` before Gradle runs.

If `GOOGLE_SERVICES_JSON` is missing in EAS, the Android build fails because `google-services.json` is not tracked by git and is therefore not uploaded with the build context.

---

## 📱 Strategies for Sharing Code (Web & Mobile)

You asked: *"If we create a React Native app, how can we use components so that if a change is needed, I update it only in one place?"*

This is the key advantage of a Monorepo. However, **React (Web)** and **React Native (Mobile)** use different primitives (`<div>` vs `<View>`).

### How we achieve "Write Once, Update Everywhere":

#### 1. Shared Business Logic (The "Brain")
We extract all **non-visual** code into shared libraries.
*   **API Calls & Services**: `libs/shared/api`
*   **State Management (Zustand/Redux)**: `libs/shared/store`
*   **Hooks & Utilities**: `libs/shared/hooks`
*   **Types/Interfaces**: `libs/shared/types`

*If you change a validation rule or API endpoint in `libs`, it updates for BOTH Web and Mobile instantly.*

#### 2. Shared UI (The "Look")
Since HTML and Native UI differ, we have two main strategies:

*   **Strategy A (Universal Components):** Use a library like **Tamagui** or **Solito**. These libraries let you write ``<Stack>`` or ``<Text>`` which compiles to `div` on web and `View` on mobile. This allows 90% code sharing.
*   **Strategy B (Separate UI, Shared Tokens):** We keep `apps/web` components and `apps/mobile` components separate but share **Design Tokens** (colors, spacing, typography) in `libs/shared-ui`.
    *   *Why?* Web often needs hover states, responsive grids, and cursor interactions that don't exist on mobile. Mobile needs gestures and native transitions.

**Recommended Approach for SwiftTrack:**
We currently use **Shadcn/UI** (Web-only). To support effortless updates across platforms in the future, we should:
1.  Move core constants (Colors, Strings) to `libs/shared-ui`.
2.  Extract complex business logic (e.g., "Calculate distance", "Format currency") to `libs/shared-utils`.

### Creating a New Shared Library
To create a new library for sharing code:

```bash
# Create a library for generic logic
npx nx g @nx/js:lib shared-logic --directory=libs/shared-logic

# Create a library for UI components
npx nx g @nx/react:lib shared-ui --directory=libs/shared-ui
```

---

## 🛠 Project Configuration (Nx)

If you are wondering *"Why was it not configured?"*, we are using **Nx Project Crystal**.
*   Nx now **infers** targets from your `package.json` scripts automatically.
*   You don't need a massive `project.json` file anymore.
*   If `apps/web/package.json` has a `"dev"` script, Nx automatically creates a `web:dev` target for you.

You can verify this by running:
```bash
npx nx show project web
```

---

## ✅ Summary

1.  **Monorepo is active**: We have the Web and Expo Mobile app structure ready.
2.  **Use `npm install`**: Run this from the root directory to install packages for all apps.
3.  **Use `npx nx dev web`**: This runs the web dashboard.
4.  **Use `npx nx start mobileapp`** (or `cd apps/mobileapp && npx expo start`): This runs the Expo mobile app.
5.  **Shared Logic**: Both apps can reuse logic and UI from `libs/`.
