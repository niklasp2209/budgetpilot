"use client";

import { useEffect, type ReactNode } from "react";
import { useRouter } from "next/navigation";
import { getStoredTokens } from "@/shared/lib/storage";
import { useAuth } from "@/features/auth/context/AuthProvider";
import { useTranslation } from "@/features/i18n/context/I18nProvider";

type AuthGuardProps = Readonly<{
  children: ReactNode;
}>;

export function AuthGuard({ children }: AuthGuardProps) {
  const router = useRouter();
  const { isAuthenticated, isLoading } = useAuth();
  const { t } = useTranslation();

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
        <p className="muted">{t("auth.loadingSession")}</p>
      </div>
    );
  }

  return children;
}
