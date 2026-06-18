"use client";

import { FormEvent, useState } from "react";
import { ApiError } from "@/shared/api/client";
import { slugify } from "@/shared/lib/format";
import { useOrganization } from "@/features/organization/context/OrganizationProvider";

export function CreateOrganizationForm() {
  const { createFirstOrganization } = useOrganization();
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
        setError("Failed to create organization.");
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="card setup-card" onSubmit={handleSubmit}>
      <h2>Create your first organization</h2>
      <p className="muted">You need an organization before the dashboard can load data.</p>
      {error ? <p className="error">{error}</p> : null}
      <label>
        Name
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
        Slug
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
        {isSubmitting ? "Creating..." : "Create organization"}
      </button>
    </form>
  );
}
