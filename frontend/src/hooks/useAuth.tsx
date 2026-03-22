import {createContext, type ReactNode, useCallback, useContext, useEffect, useState,} from 'react';
import type {UserResponse} from '@/types';
import {ApiError, authApi} from '@/services/api';

interface AuthContextValue {
  user: UserResponse | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  isAdmin: boolean;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    authApi
      .me()
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
  }, []);

  const login = useCallback(async (username: string, password: string) => {
    const loggedInUser = await authApi.login({ username, password });
    setUser(loggedInUser);
  }, []);

  const logout = useCallback(async () => {
    try {
      await authApi.logout();
    } catch (error) {
      if (error instanceof ApiError && error.status !== 401) {
        throw error;
      }
    }
    setUser(null);
  }, []);

  const isAdmin = user?.role === 'ADMINISTRATOR';

  return (
    <AuthContext value={{ user, loading, login, logout, isAdmin }}>
      {children}
    </AuthContext>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return ctx;
}
