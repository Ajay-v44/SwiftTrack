'use client';

import { useEffect } from 'react';
import { requestForToken, onMessageListener } from '@/lib/firebase';
import { toast } from 'sonner';

import { useAuthStore } from '@/store/useAuthStore';

export function NotificationManager() {
  const { user } = useAuthStore();

  useEffect(() => {
    if (user && user.id) {
       // Proceed with generating token if user is signed in
       // Handle explicit browser permission requesting 
       if (!('Notification' in window)) {
         console.error("[NotificationManager] This browser does not support notifications.");
       } else if (Notification.permission === 'granted') {
         console.log("[NotificationManager] Permission is 'granted'. Silently fetching token...");
         requestForToken(user.id, user.tenantId || undefined);
       } else if (Notification.permission === 'default') {
         console.log("[NotificationManager] Permission is 'default'. Showing interactive Setup toast...");
         toast('Enable Push Notifications', {
           description: 'Please allow push notifications to receive live updates.',
           duration: 15000,
           action: {
             label: 'Setup',
             onClick: () => {
               console.log("[NotificationManager] User clicked Setup button in toast. Requesting permission natively...");
               Notification.requestPermission().then((permission) => {
                 console.log("[NotificationManager] User answered prompt natively. New permission status:", permission);
                 if (permission === 'granted') {
                   requestForToken(user.id, user.tenantId || undefined);
                   toast.success("Notifications enabled!");
                 } else {
                   console.warn("[NotificationManager] User denied prompt natively! Token generation aborted.");
                   toast.error("Notifications blocked by user.");
                 }
               });
             },
           },
         });
       } else {
         console.warn("[NotificationManager] Permission is permanently '" + Notification.permission + "'. Browsers require you to manually click the URL padlock to unblock this site.");
       }

       // Handle foreground messaging
       let unsubscribe: (() => void) | undefined;
       onMessageListener((payload: any) => {
         if (payload?.notification) {
           toast(payload.notification.title, {
             description: payload.notification.body,
           });

           const audio = new Audio('/notification.mp3');
           audio.play().catch(e => console.log('Audio play failed:', e));

           window.dispatchEvent(
             new CustomEvent('onFirebaseMessage', { detail: payload })
           );
         }
       }).then(unsub => {
         unsubscribe = unsub;
       }).catch((err) => console.log('failed: ', err));

       return () => {
         if (unsubscribe) unsubscribe();
       };
    }
  }, [user?.id]);

  return null;
}
