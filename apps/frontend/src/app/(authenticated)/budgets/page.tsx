"use client";

import { BudgetsView } from "@/features/budgets/components/BudgetsView";
import { useTranslation } from "@/features/i18n/context/I18nProvider";
import { OrganizationGate } from "@/features/organization/components/OrganizationGate";
import { useOrganization } from "@/features/organization/context/OrganizationProvider";

export default function BudgetsPage() {
  const { selectedOrganization } = useOrganization();
  const { t } = useTranslation();

  return (
    <OrganizationGate>
      <div className="page-content">
        <div className="page-heading">
          <h1>{t("pages.budgetsTitle")}</h1>
          <p className="muted">
            {t("pages.budgetsSubtitle", { name: selectedOrganization?.name ?? "" })}
          </p>
        </div>
        <BudgetsView />
      </div>
    </OrganizationGate>
  );
}
