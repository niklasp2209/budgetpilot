"use client";

import type { ReactNode } from "react";
import { AuthGuard } from "@/features/auth/components/AuthGuard";
import { useAuth } from "@/features/auth/context/AuthProvider";
import { LanguageSwitcher } from "@/features/i18n/components/LanguageSwitcher";
import { useTranslation } from "@/features/i18n/context/I18nProvider";
import { AppNav } from "@/features/organization/components/AppNav";
import { OrganizationPicker } from "@/features/organization/components/OrganizationPicker";
import { OrganizationProvider } from "@/features/organization/context/OrganizationProvider";

type AuthenticatedLayoutProps = Readonly<{
  children: ReactNode;
}>;

function AuthenticatedShell({ children }: AuthenticatedLayoutProps) {
  const { email, logout } = useAuth();
  const { t } = useTranslation();

  return (
    <div className="app-shell">
      <header className="app-header">
        <div>
          <p className="brand">BudgetPilot</p>
          <p className="muted">{email}</p>
        </div>
        <div className="header-actions">
          <LanguageSwitcher />
          <OrganizationPicker />
          <button type="button" className="secondary-button header-button" onClick={logout}>
            {t("auth.logout")}
          </button>
        </div>
      </header>
      <AppNav />
      <main className="app-main">{children}</main>
    </div>
  );
}

export default function AuthenticatedLayout({ children }: AuthenticatedLayoutProps) {
  return (
    <AuthGuard>
      <OrganizationProvider>
        <AuthenticatedShell>{children}</AuthenticatedShell>
      </OrganizationProvider>
    </AuthGuard>
  );
}
