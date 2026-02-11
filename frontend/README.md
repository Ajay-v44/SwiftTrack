# SwiftTrack Frontend Monorepo

Welcome to the **SwiftTrack** frontend workspace. This project is built as a **Monorepo** using [Nx](https://nx.dev), allowing us to manage multiple applications (Web, Mobile) and shared libraries in a single codebase.

## 📂 Canvas Structure

The workspace is organized as follows:

- **apps/**: Contains the main application entry points.
  - **web/**: The Next.js dashboard and landing page (`d:\SwiftTrack\frontend\apps\web`).
  - *(Future)* **mobile/**: The React Native application for drivers/users.
- **libs/**: Contains shared code used across applications.
  - **shared-ui/**: Reusable UI components, hooks, and utilities.
- **tools/**: Custom scripts and workspace configurations.

---

## 🚀 Running Tasks

We use **Nx** to run tasks efficiently. While you can run `npm run dev` inside specific folders, using `nx` commands from the root provides caching and better orchestration.

### Common Commands

| Task | Command | Description |
|------|---------|-------------|
| **Start Dev Server** | `npx nx dev web` | Starts the Next.js web application locally. |
| **Build** | `npx nx build web` | Builds the web application for production. |
| **Lint** | `npx nx lint web` | Runs ESLint checks. |
| **Test** | `npx nx test web` | Runs unit tests (if configured). |

> **Note:** Just run `npx nx dev web` from the root `d:\SwiftTrack\frontend` folder!

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

1.  **Monorepo is active**: We have the structure ready.
2.  **Use `npx nx dev web`**: This is the correct way to run the app.
3.  **Future Mobile App**: We will create `apps/mobile` and reuse logic from `libs/`.
