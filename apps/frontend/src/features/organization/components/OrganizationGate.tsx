"use client";

import type { ReactNode } from "react";
import { CreateOrganizationForm } from "@/features/organization/components/CreateOrganizationForm";
import { useOrganization } from "@/features/organization/context/OrganizationProvider";
import { useTranslation } from "@/features/i18n/context/I18nProvider";

type OrganizationGateProps = Readonly<{
  children: ReactNode;
}>;

export function OrganizationGate({ children }: OrganizationGateProps) {
  const { organizations, isLoading, error } = useOrganization();
  const { t } = useTranslation();

  if (isLoading) {
    return <p className="muted">{t("org.loadingOrganizations")}</p>;
  }

  if (error) {
    return <p className="error">{error}</p>;
  }

  if (organizations.length === 0) {
    return <CreateOrganizationForm />;
  }

  return children;
}
