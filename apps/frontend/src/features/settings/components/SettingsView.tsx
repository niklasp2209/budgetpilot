"use client";

import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { ApiError } from "@/shared/api/client";
import { changePassword, deleteOrganization, updateOrganization } from "@/shared/api/organizations";
import { DEFAULT_ORGANIZATION_CURRENCY } from "@/shared/lib/currencies";
import { slugify } from "@/shared/lib/format";
import { useAuth } from "@/features/auth/context/AuthProvider";
import { CurrencySelect } from "@/features/organization/components/CurrencySelect";
import { useTranslation } from "@/features/i18n/context/I18nProvider";
import { useOrganization } from "@/features/organization/context/OrganizationProvider";

type SettingsTab = "profile" | "organization";

export function SettingsView() {
  const router = useRouter();
  const { email } = useAuth();
  const { selectedOrganization, reload } = useOrganization();
  const { t } = useTranslation();
  const [activeTab, setActiveTab] = useState<SettingsTab>("profile");
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [orgName, setOrgName] = useState("");
  const [orgSlug, setOrgSlug] = useState("");
  const [orgCurrency, setOrgCurrency] = useState<string>(DEFAULT_ORGANIZATION_CURRENCY);
  const [slugTouched, setSlugTouched] = useState(false);

  const canManageOrg =
    selectedOrganization?.role === "OWNER" || selectedOrganization?.role === "ADMIN";
  const canDeleteOrg = selectedOrganization?.role === "OWNER";

  useEffect(() => {
    if (!canManageOrg && activeTab === "organization") {
      setActiveTab("profile");
    }
  }, [canManageOrg, activeTab]);

  useEffect(() => {
    if (!selectedOrganization) {
      return;
    }
    setOrgName(selectedOrganization.name);
    setOrgSlug(selectedOrganization.slug);
    setOrgCurrency(selectedOrganization.currency);
    setSlugTouched(false);
  }, [selectedOrganization?.id, selectedOrganization?.name, selectedOrganization?.slug, selectedOrganization?.currency]);

  function switchTab(tab: SettingsTab) {
    setActiveTab(tab);
    setError(null);
    setSuccess(null);
  }

  async function handleChangePassword(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSuccess(null);
    if (newPassword !== confirmPassword) {
      setError(t("settings.passwordMismatch"));
      return;
    }
    try {
      await changePassword(currentPassword, newPassword);
      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
      setSuccess(t("settings.passwordChanged"));
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : t("settings.passwordChangeFailed"));
    }
  }

  async function handleUpdateOrganization(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedOrganization) {
      return;
    }
    setError(null);
    setSuccess(null);
    try {
      await updateOrganization(selectedOrganization.id, orgName.trim(), orgSlug.trim(), orgCurrency);
      await reload();
      setSuccess(t("settings.organizationUpdated"));
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : t("settings.updateOrganizationFailed"));
    }
  }

  async function handleDeleteOrganization() {
    if (!selectedOrganization || !canDeleteOrg) {
      return;
    }
    if (!window.confirm(t("settings.deleteOrganizationConfirm"))) {
      return;
    }
    setError(null);
    setSuccess(null);
    try {
      await deleteOrganization(selectedOrganization.id);
      await reload();
      router.replace("/dashboard");
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : t("settings.deleteOrganizationFailed"));
    }
  }

  return (
    <div className="stack">
      {canManageOrg ? (
        <div className="settings-tabs" role="tablist" aria-label={t("settings.title")}>
          <button
            type="button"
            role="tab"
            aria-selected={activeTab === "profile"}
            className={activeTab === "profile" ? "settings-tab active" : "settings-tab"}
            onClick={() => switchTab("profile")}
          >
            {t("settings.tabProfile")}
          </button>
          <button
            type="button"
            role="tab"
            aria-selected={activeTab === "organization"}
            className={activeTab === "organization" ? "settings-tab active" : "settings-tab"}
            onClick={() => switchTab("organization")}
          >
            {t("settings.tabOrganization")}
          </button>
        </div>
      ) : null}

      {error ? <p className="error">{error}</p> : null}
      {success ? <p className="success">{success}</p> : null}

      {activeTab === "profile" ? (
        <section className="card card-wide">
          <h2>{t("settings.profile")}</h2>
          <p className="muted">{t("settings.profileDescription")}</p>
          <dl className="metric-list">
            <div>
              <dt>{t("common.email")}</dt>
              <dd>{email}</dd>
            </div>
          </dl>
          <form className="stack-form" onSubmit={handleChangePassword}>
            <h3>{t("settings.changePassword")}</h3>
            <label>
              {t("settings.currentPassword")}
              <input
                type="password"
                value={currentPassword}
                onChange={(event) => setCurrentPassword(event.target.value)}
                required
                autoComplete="current-password"
              />
            </label>
            <label>
              {t("settings.newPassword")}
              <input
                type="password"
                value={newPassword}
                onChange={(event) => setNewPassword(event.target.value)}
                required
                minLength={8}
                autoComplete="new-password"
              />
            </label>
            <label>
              {t("settings.confirmPassword")}
              <input
                type="password"
                value={confirmPassword}
                onChange={(event) => setConfirmPassword(event.target.value)}
                required
                minLength={8}
                autoComplete="new-password"
              />
            </label>
            <button type="submit" className="inline-button">
              {t("settings.changePassword")}
            </button>
          </form>
        </section>
      ) : null}

      {activeTab === "organization" && canManageOrg && selectedOrganization ? (
        <section className="card card-wide">
          <h2>{t("settings.organization")}</h2>
          <p className="muted">{t("settings.organizationDescription")}</p>
          <form className="stack-form" onSubmit={handleUpdateOrganization}>
            <label>
              {t("common.name")}
              <input
                type="text"
                value={orgName}
                onChange={(event) => {
                  const nextName = event.target.value;
                  setOrgName(nextName);
                  if (!slugTouched) {
                    setOrgSlug(slugify(nextName));
                  }
                }}
                required
              />
            </label>
            <label>
              {t("common.slug")}
              <input
                type="text"
                value={orgSlug}
                onChange={(event) => {
                  setSlugTouched(true);
                  setOrgSlug(event.target.value);
                }}
                required
              />
            </label>
            <CurrencySelect value={orgCurrency} onChange={setOrgCurrency} />
            <div className="form-actions">
              <button type="submit">{t("settings.updateOrganization")}</button>
              {canDeleteOrg ? (
                <button
                  type="button"
                  className="danger-button"
                  onClick={() => void handleDeleteOrganization()}
                >
                  {t("settings.deleteOrganization")}
                </button>
              ) : null}
            </div>
          </form>
        </section>
      ) : null}
    </div>
  );
}
