# Notification Service

The **Notification Service** is responsible for delivering targeted push notifications and broad broadcast events to the users (Drivers, Tenants, etc.) using **Firebase Cloud Messaging (FCM)**.

This service correctly implements the **FCM HTTP v1 API** (through the use of `firebase-admin` > `v9.x.x`), which is required as of June 2024 to replace the legacy FCM API endpoints.

## Features

- **Store Device Tokens:** Front-end applications pass FCM tokens via the `/api/notifications/token` endpoint. The NotificationService maps them to the specific `UserId` inside Google Firestore.
- **Direct User Notifications:** Sends notifications to independent users leveraging the tokens stored in Firestore.
- **Tenant Scope Messaging (Topics):** Leverages FCM Topics (e.g. `tenant_{tenantId}`) allowing broad delivery. When a tenant is registered, the FCM token is hooked to this topic so event dispatches inside Kafka trigger an immediate group alert without querying the DB for 1000s of tokens.
- **Kafka Integrated:** Listens natively to `order-created`, `driver-assigned`, and `order-delivered` events using Spring Kafka.

## Configuration & Environment

### Setting up Firebase Authentication
The `NotificationService` handles the Admin SDK via standard JSON service accounts securely linked through environment variables without hardcoding.

Inside `/home/ajay/Ajay/Personal/SwiftTrack/backend/services/NotificationService/`, place your generated Admin JSON:
- File name: `swifttrcak-firebase-adminsdk.json`

Check your `.env` configuration file in that directory to ensure `FIREBASE_CONFIG_PATH` is pointing precisely to this Admin JSON.

```env
# /backend/services/NotificationService/.env
FIREBASE_CONFIG_PATH=swifttrcak-firebase-adminsdk.json
SERVER_PORT=8011
EUREKA_URL=http://127.0.0.1:8761/eureka/
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

### Note on Firebase MCP
We explored using `firebase mcp` automated configuration, but due to lack of authenticated context inside the local machine (`Authenticated User: <NONE>`), we could not retrieve web SDK scripts or set active project variables. 

To use `firebase mcp` properly for this project in the future:
1. Ensure you have run: `firebase login:ci` or standard `firebase login` on your system.
2. In MCP environments, the `firebase_login` tool handles manual Google OAuth redirects, which requires interacting with a browser manually for security token delegation.

## Frontend Setup 

Latest structural modifications implemented over the React + Next.js client (`frontend/apps/web/`):
1. **Service Worker (`public/firebase-messaging-sw.js`):** 
Updated to handle modern background payloads via Firebase Web SDK compat bundles. Next.js doesn't parse local env variables safely within `public/` directories dynamically during runtime without proxy handlers, so please update `YOUR_API_KEY`, etc. dynamically using environment scripts or manual Webpack replacements directly.
   
2. **Foreground Observer (`lib/firebase.ts` & `NotificationManager.tsx`):**
Requests tracking permission cleanly onto the browser `navigator`. Once acquired, automatically sends the DeviceToken array (`token`, `userId`, `userType`, `tenantId`) over to `/api/notifications/token`.

> Ensure your `.env.local` inside `/frontend/apps/web/` holds all `NEXT_PUBLIC_FIREBASE_*` properties correctly matching your Firebase console.

## Important Note on the Latest Firebase Web Changes (2024/2025):
- Firebase completely sunsetted **Legacy FCM APIs** on June 20, 2024. Your backend Java Admin SDK version is officially shifted towards `9.8.0` (which by default translates under the hood to `v1 HTTP API`) maintaining perfect modern compliance.
- From SDK 11.x forwards (Modular implementation format is encouraged), you can convert the Service Worker inside the App Directory away from relying completely on compat `importScripts` for lighter payload optimizations.
- The `vapidKey` integration remains strongly enforced when compiling `.getToken()`. Make sure it's provisioned in Firebase under Web Push Certificates!
