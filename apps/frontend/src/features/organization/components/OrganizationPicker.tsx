"use client";

import { useOrganization } from "@/features/organization/context/OrganizationProvider";

export function OrganizationPicker() {
  const { organizations, selectedOrganization, selectOrganization } = useOrganization();

  if (organizations.length <= 1) {
    return selectedOrganization ? (
      <span className="org-badge">{selectedOrganization.name}</span>
    ) : null;
  }

  return (
    <label className="org-picker">
      Organization
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
