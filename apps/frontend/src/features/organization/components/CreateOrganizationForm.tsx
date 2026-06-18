"use client";

import { FormEvent, useState } from "react";
import { ApiError } from "@/shared/api/client";
import { slugify } from "@/shared/lib/format";
import { useOrganization } from "@/features/organization/context/OrganizationProvider";
import { useTranslation } from "@/features/i18n/context/I18nProvider";

export function CreateOrganizationForm() {
  const { createFirstOrganization } = useOrganization();
  const { t } = useTranslation();
  const [name, setName] = useState("");
  const [slug, setSlug] = useState("");
  const [slugTouched, setSlugTouched] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      await createFirstOrganization(name.trim(), slug.trim());
    } catch (caught) {
      if (caught instanceof ApiError) {
        setError(caught.message);
      } else {
        setError(t("org.createFailed"));
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="card setup-card" onSubmit={handleSubmit}>
      <h2>{t("org.createFirstTitle")}</h2>
      <p className="muted">{t("org.createFirstDescription")}</p>
      {error ? <p className="error">{error}</p> : null}
      <label>
        {t("common.name")}
        <input
          type="text"
          value={name}
          onChange={(event) => {
            const nextName = event.target.value;
            setName(nextName);
            if (!slugTouched) {
              setSlug(slugify(nextName));
            }
          }}
          required
        />
      </label>
      <label>
        {t("common.slug")}
        <input
          type="text"
          value={slug}
          onChange={(event) => {
            setSlugTouched(true);
            setSlug(event.target.value);
          }}
          required
        />
      </label>
      <button type="submit" disabled={isSubmitting}>
        {isSubmitting ? t("org.creating") : t("org.createOrganization")}
      </button>
    </form>
  );
}
