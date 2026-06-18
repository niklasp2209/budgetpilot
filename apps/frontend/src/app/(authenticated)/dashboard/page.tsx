"use client";

import { DashboardView } from "@/features/dashboard/components/DashboardView";
import { OrganizationGate } from "@/features/organization/components/OrganizationGate";
import { useOrganization } from "@/features/organization/context/OrganizationProvider";

export default function DashboardPage() {
  const { selectedOrganization } = useOrganization();

  return (
    <OrganizationGate>
      <div className="page-content">
        <div className="page-heading">
          <h1>Dashboard</h1>
          <p className="muted">Overview for {selectedOrganization?.name}.</p>
        </div>
        <DashboardView />
      </div>
    </OrganizationGate>
  );
}
