"use client";

import { AccountingView } from "@/features/accounting/components/AccountingView";
import { OrganizationGate } from "@/features/organization/components/OrganizationGate";
import { useOrganization } from "@/features/organization/context/OrganizationProvider";

export default function AccountingPage() {
  const { selectedOrganization } = useOrganization();

  return (
    <OrganizationGate>
      <div className="page-content">
        <div className="page-heading">
          <h1>Accounting</h1>
          <p className="muted">Accounts, categories, and transactions for {selectedOrganization?.name}.</p>
        </div>
        <AccountingView />
      </div>
    </OrganizationGate>
  );
}
