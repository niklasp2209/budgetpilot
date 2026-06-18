"use client";

import type { ReactNode } from "react";
import { AuthProvider } from "@/features/auth/context/AuthProvider";
import { I18nProvider } from "@/features/i18n/context/I18nProvider";

type AppProvidersProps = Readonly<{
  children: ReactNode;
}>;

export function AppProviders({ children }: AppProvidersProps) {
  return (
    <I18nProvider>
      <AuthProvider>{children}</AuthProvider>
    </I18nProvider>
  );
}
