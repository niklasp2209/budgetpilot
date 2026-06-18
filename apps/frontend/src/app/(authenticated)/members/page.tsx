"use client";

import { MembersView } from "@/features/members/components/MembersView";
import { useTranslation } from "@/features/i18n/context/I18nProvider";
import { OrganizationGate } from "@/features/organization/components/OrganizationGate";
import { useOrganization } from "@/features/organization/context/OrganizationProvider";

export default function MembersPage() {
  const { selectedOrganization } = useOrganization();
  const { t } = useTranslation();

  return (
    <OrganizationGate>
      <div className="page-content">
        <div className="page-heading">
          <h1>{t("pages.membersTitle")}</h1>
          <p className="muted">
            {t("pages.membersSubtitle", { name: selectedOrganization?.name ?? "" })}
          </p>
        </div>
        <MembersView />
      </div>
    </OrganizationGate>
  );
}
