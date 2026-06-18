"use client";

import { useOrganization } from "@/features/organization/context/OrganizationProvider";
import { useTranslation } from "@/features/i18n/context/I18nProvider";

export function OrganizationPicker() {
  const { organizations, selectedOrganization, selectOrganization } = useOrganization();
  const { t } = useTranslation();

  if (organizations.length <= 1) {
    return selectedOrganization ? (
      <span className="org-badge">{selectedOrganization.name}</span>
    ) : null;
  }

  return (
    <label className="org-picker">
      {t("common.organization")}
      <select
        value={selectedOrganization?.id ?? ""}
        onChange={(event) => selectOrganization(event.target.value)}
      >
        {organizations.map((organization) => (
          <option key={organization.id} value={organization.id}>
            {organization.name}
          </option>
        ))}
      </select>
    </label>
  );
}
