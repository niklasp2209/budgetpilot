"use client";

import { BudgetsView } from "@/features/budgets/components/BudgetsView";
import { OrganizationGate } from "@/features/organization/components/OrganizationGate";
import { useOrganization } from "@/features/organization/context/OrganizationProvider";

export default function BudgetsPage() {
  const { selectedOrganization } = useOrganization();

  return (
    <OrganizationGate>
      <div className="page-content">
        <div className="page-heading">
          <h1>Budgets</h1>
          <p className="muted">Monthly budgets for {selectedOrganization?.name}.</p>
        </div>
        <BudgetsView />
      </div>
    </OrganizationGate>
  );
}
