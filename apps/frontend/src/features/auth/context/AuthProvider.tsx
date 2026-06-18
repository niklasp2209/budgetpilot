"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode
} from "react";
import { useRouter } from "next/navigation";
import { loginRequest, registerRequest } from "@/shared/api/client";
import {
  clearStoredOrganizationId,
  clearStoredTokens,
  getStoredTokens,
  setStoredTokens
} from "@/shared/lib/storage";
import { fetchMe } from "@/shared/api/organizations";

type AuthContextValue = {
  email: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string) => Promise<void>;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

type AuthProviderProps = Readonly<{
  children: ReactNode;
}>;

export function AuthProvider({ children }: AuthProviderProps) {
  const router = useRouter();
  const [email, setEmail] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let active = true;

    async function bootstrapSession() {
      const stored = getStoredTokens();
      if (!stored) {
        if (active) {
          setIsLoading(false);
        }
        return;
      }

      try {
        const me = await fetchMe();
        if (active) {
          setEmail(me.email);
        }
      } catch {
        clearStoredTokens();
        if (active) {
          setEmail(null);
        }
      } finally {
        if (active) {
          setIsLoading(false);
        }
      }
    }

    void bootstrapSession();

    return () => {
      active = false;
    };
  }, []);

  const login = useCallback(async (loginEmail: string, password: string) => {
    const tokens = await loginRequest(loginEmail, password);
    setStoredTokens({
      accessToken: tokens.accessToken,
      refreshToken: tokens.refreshToken
    });
    const me = await fetchMe();
    setEmail(me.email);
    router.push("/dashboard");
  }, [router]);

  const register = useCallback(async (registerEmail: string, password: string) => {
    const tokens = await registerRequest(registerEmail, password);
    setStoredTokens({
      accessToken: tokens.accessToken,
      refreshToken: tokens.refreshToken
    });
    const me = await fetchMe();
    setEmail(me.email);
    router.push("/dashboard");
  }, [router]);

  const logout = useCallback(() => {
    clearStoredTokens();
    clearStoredOrganizationId();
    setEmail(null);
    router.push("/login");
  }, [router]);

  const value = useMemo(
    () => ({
      email,
      isAuthenticated: email !== null,
      isLoading,
      login,
      register,
      logout
    }),
    [email, isLoading, login, register, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider.");
  }
  return context;
}
