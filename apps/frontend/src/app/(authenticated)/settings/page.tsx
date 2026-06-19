"use client";

import { SettingsView } from "@/features/settings/components/SettingsView";
import { useTranslation } from "@/features/i18n/context/I18nProvider";

export default function SettingsPage() {
  const { t } = useTranslation();

  return (
    <div className="page-content">
      <div className="page-heading">
        <h1>{t("settings.title")}</h1>
        <p className="muted">{t("settings.subtitle")}</p>
      </div>
      <SettingsView />
    </div>
  );
}
