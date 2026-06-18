"use client";

import type { ReactNode } from "react";
import { AuthProvider } from "@/features/auth/context/AuthProvider";

type AppProvidersProps = Readonly<{
  children: ReactNode;
}>;

export function AppProviders({ children }: AppProvidersProps) {
  return <AuthProvider>{children}</AuthProvider>;
}
