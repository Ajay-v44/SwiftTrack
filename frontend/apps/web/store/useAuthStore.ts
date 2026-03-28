import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import Cookies from 'js-cookie';
import type { UserDetails } from '@swifttrack/types';

interface AuthState {
  user: UserDetails | null;
  token: string | null;
  isLoading: boolean;
  setAuth: (token: string) => void;
  setUser: (user: UserDetails) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      token: null,
      isLoading: false,
      setAuth: (token) => {
        Cookies.set('auth_token', token, { expires: 7 });
        set({ token });
      },
      setUser: (user) => set({ user }),
      logout: () => {
        Cookies.remove('auth_token');
        set({ user: null, token: null });
      },
    }),
    {
      name: 'swifttrack-auth',
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({
        user: state.user,
        token: state.token || Cookies.get('auth_token') || null,
        isLoading: false,
      }),
    }
  )
);
