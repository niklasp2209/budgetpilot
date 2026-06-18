"use client";

import type { ReactNode } from "react";
import { AuthGuard } from "@/features/auth/components/AuthGuard";
import { useAuth } from "@/features/auth/context/AuthProvider";
import { OrganizationPicker } from "@/features/organization/components/OrganizationPicker";
import { OrganizationProvider } from "@/features/organization/context/OrganizationProvider";

type DashboardLayoutProps = Readonly<{
  children: ReactNode;
}>;

function DashboardShell({ children }: DashboardLayoutProps) {
  const { email, logout } = useAuth();

  return (
    <div className="app-shell">
      <header className="app-header">
        <div>
          <p className="brand">BudgetPilot</p>
          <p className="muted">{email}</p>
        </div>
        <div className="header-actions">
          <OrganizationPicker />
          <button type="button" className="secondary-button" onClick={logout}>
            Logout
          </button>
        </div>
      </header>
      <main className="app-main">{children}</main>
    </div>
  );
}

export default function DashboardLayout({ children }: DashboardLayoutProps) {
  return (
    <AuthGuard>
      <OrganizationProvider>
        <DashboardShell>{children}</DashboardShell>
      </OrganizationProvider>
    </AuthGuard>
  );
}
