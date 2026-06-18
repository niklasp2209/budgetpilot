"use client";

import { DashboardView } from "@/features/dashboard/components/DashboardView";
import { useTranslation } from "@/features/i18n/context/I18nProvider";
import { OrganizationGate } from "@/features/organization/components/OrganizationGate";
import { useOrganization } from "@/features/organization/context/OrganizationProvider";

export default function DashboardPage() {
  const { selectedOrganization } = useOrganization();
  const { t } = useTranslation();

  return (
    <OrganizationGate>
      <div className="page-content">
        <div className="page-heading">
          <h1>{t("pages.dashboardTitle")}</h1>
          <p className="muted">
            {t("pages.dashboardSubtitle", { name: selectedOrganization?.name ?? "" })}
          </p>
        </div>
        <DashboardView />
      </div>
    </OrganizationGate>
  );
}
