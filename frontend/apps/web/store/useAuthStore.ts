import { create } from 'zustand';
import Cookies from 'js-cookie';

export type UserType = 
| 'SUPER_ADMIN' 
| 'SYSTEM_ADMIN' 
| 'SYSTEM_USER' 
| 'ADMIN_USER' 
| 'TENANT_ADMIN' 
| 'TENANT_USER' 
| 'TENANT_DRIVER' 
| 'TENANT_MANAGER' 
| 'TENANT_STAFF' 
| 'DRIVER_USER' 
| 'CONSUMER' 
| 'PROVIDER_USER' 
| 'PROVIDER_ADMIN';

export interface UserDetails {
  id: string;
  tenantId?: string | null;
  providerId?: string | null;
  type: UserType;
  name: string;
  mobile: string;
  roles: string[];
}

interface AuthState {
  user: UserDetails | null;
  token: string | null;
  isLoading: boolean;
  setAuth: (token: string) => void;
  setUser: (user: UserDetails) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  token: Cookies.get('auth_token') || null,
  isLoading: false,
  setAuth: (token) => {
    Cookies.set('auth_token', token, { expires: 7 }); // expires in 7 days
    set({ token });
  },
  setUser: (user) => set({ user }),
  logout: () => {
    Cookies.remove('auth_token');
    set({ user: null, token: null });
  },
}));
