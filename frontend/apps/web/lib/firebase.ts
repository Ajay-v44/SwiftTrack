import { initializeApp } from "firebase/app";
import { getMessaging, getToken, onMessage, isSupported } from "firebase/messaging";

const firebaseConfig = {
  apiKey: process.env.NEXT_PUBLIC_FIREBASE_API_KEY,
  authDomain: process.env.NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN,
  projectId: process.env.NEXT_PUBLIC_FIREBASE_PROJECT_ID,
  storageBucket: process.env.NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: process.env.NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID,
  appId: process.env.NEXT_PUBLIC_FIREBASE_APP_ID,
};

let app: any = null;
if (firebaseConfig.projectId) {
  app = initializeApp(firebaseConfig);
}

export const messaging = async () => {
  if (!app) {
    console.warn("Firebase app not initialized. Missing config.");
    return null;
  }
  const supported = await isSupported();
  if (!supported) return null;
  return getMessaging(app);
};

export const requestForToken = async (userId: string, tenantId?: string) => {
  try {
    console.log('Requesting token for user', userId, tenantId);
    const msg = await messaging();
    if (!msg) return;

    let registration;
    if ('serviceWorker' in navigator) {
      const swUrl = `/firebase-messaging-sw.js?apiKey=${firebaseConfig.apiKey}&authDomain=${firebaseConfig.authDomain}&projectId=${firebaseConfig.projectId}&storageBucket=${firebaseConfig.storageBucket}&messagingSenderId=${firebaseConfig.messagingSenderId}&appId=${firebaseConfig.appId}`;
      await navigator.serviceWorker.register(swUrl);
      
      // CRITICAL: Next.js HMR and Firebase getToken inherently race each other. 
      // We must explicitly yield until the Service Worker finishes "installing" and upgrades to "active".
      registration = await navigator.serviceWorker.ready;
    }

    const currentToken = await getToken(msg, {
      vapidKey: process.env.NEXT_PUBLIC_FIREBASE_VAPID_KEY,
      serviceWorkerRegistration: registration
    });

    if (currentToken) {
      console.log('FCM Token generated:', currentToken);
      // Send token to our NotificationService
      await sendTokenToServer(currentToken, userId, tenantId);
    } else {
      console.warn('No registration token available.');
    }
  } catch (err: any) {
    if (err.code === 'messaging/permission-blocked') {
      console.warn('Notifications mathematically blocked by browser. User must click the padlock next to the URL and manually select "Allow".');
    } else {
      console.error('An error occurred while retrieving token. ', err);
    }
  }
};

const sendTokenToServer = async (token: string, userId: string, tenantId?: string) => {
  try {
    console.log('Sending token to server', token, userId, tenantId);
    const baseUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
    const response = await fetch(`http://localhost:8011/api/notifications/token`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        token,
        userId,
        tenantId
      }),
    });
    if (!response.ok) {
      console.error('Failed to send token to server', response.statusText);
    } else {
      console.log('Token sent to server successfully');
    }
  } catch (err) {
    console.error('Error sending token to server', err);
  }
};

export const onMessageListener = async (callback: (payload: any) => void) => {
  const msg = await messaging();
  if (!msg) return () => {};

  return onMessage(msg, (payload) => {
    console.log("Message received. ", payload);
    callback(payload);
  });
};
