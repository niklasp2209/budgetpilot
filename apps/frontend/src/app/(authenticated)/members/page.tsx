"use client";

import { MembersView } from "@/features/members/components/MembersView";
import { OrganizationGate } from "@/features/organization/components/OrganizationGate";
import { useOrganization } from "@/features/organization/context/OrganizationProvider";

export default function MembersPage() {
  const { selectedOrganization } = useOrganization();

  return (
    <OrganizationGate>
      <div className="page-content">
        <div className="page-heading">
          <h1>Members</h1>
          <p className="muted">Manage members, roles, and permission groups for {selectedOrganization?.name}.</p>
        </div>
        <MembersView />
      </div>
    </OrganizationGate>
  );
}
