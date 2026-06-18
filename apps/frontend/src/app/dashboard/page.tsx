"use client";

import { CreateOrganizationForm } from "@/features/organization/components/CreateOrganizationForm";
import { useOrganization } from "@/features/organization/context/OrganizationProvider";
import { DashboardView } from "@/features/dashboard/components/DashboardView";

export default function DashboardPage() {
  const { selectedOrganization, organizations, isLoading, error } = useOrganization();

  if (isLoading) {
    return <p className="muted">Loading organizations...</p>;
  }

  if (error) {
    return <p className="error">{error}</p>;
  }

  if (organizations.length === 0) {
    return <CreateOrganizationForm />;
  }

  return (
    <div className="dashboard-page">
      <div className="page-heading">
        <h1>Dashboard</h1>
        <p className="muted">Read-only overview for {selectedOrganization?.name}.</p>
      </div>
      <DashboardView />
    </div>
  );
}
