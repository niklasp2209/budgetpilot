"use client";

import type { ReactNode } from "react";
import { CreateOrganizationForm } from "@/features/organization/components/CreateOrganizationForm";
import { useOrganization } from "@/features/organization/context/OrganizationProvider";

type OrganizationGateProps = Readonly<{
  children: ReactNode;
}>;

export function OrganizationGate({ children }: OrganizationGateProps) {
  const { organizations, isLoading, error } = useOrganization();

  if (isLoading) {
    return <p className="muted">Loading organizations...</p>;
  }

  if (error) {
    return <p className="error">{error}</p>;
  }

  if (organizations.length === 0) {
    return <CreateOrganizationForm />;
  }

  return children;
}
