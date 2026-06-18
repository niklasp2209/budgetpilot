"use client";

import { AccountingView } from "@/features/accounting/components/AccountingView";
import { useTranslation } from "@/features/i18n/context/I18nProvider";
import { OrganizationGate } from "@/features/organization/components/OrganizationGate";
import { useOrganization } from "@/features/organization/context/OrganizationProvider";

export default function AccountingPage() {
  const { selectedOrganization } = useOrganization();
  const { t } = useTranslation();

  return (
    <OrganizationGate>
      <div className="page-content">
        <div className="page-heading">
          <h1>{t("pages.accountingTitle")}</h1>
          <p className="muted">
            {t("pages.accountingSubtitle", { name: selectedOrganization?.name ?? "" })}
          </p>
        </div>
        <AccountingView />
      </div>
    </OrganizationGate>
  );
}
