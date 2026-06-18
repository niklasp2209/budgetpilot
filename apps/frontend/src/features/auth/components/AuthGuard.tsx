"use client";

import { useEffect, type ReactNode } from "react";
import { useRouter } from "next/navigation";
import { getStoredTokens } from "@/shared/lib/storage";
import { useAuth } from "@/features/auth/context/AuthProvider";

type AuthGuardProps = Readonly<{
  children: ReactNode;
}>;

export function AuthGuard({ children }: AuthGuardProps) {
  const router = useRouter();
  const { isAuthenticated, isLoading } = useAuth();

  useEffect(() => {
    if (isLoading) {
      return;
    }
    if (!getStoredTokens() || !isAuthenticated) {
      router.replace("/login");
    }
  }, [isAuthenticated, isLoading, router]);

  if (isLoading || !isAuthenticated) {
    return (
      <div className="page-center">
        <p className="muted">Loading session...</p>
      </div>
    );
  }

  return children;
}
