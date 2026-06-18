"use client";

import { FormEvent, useState } from "react";
import Link from "next/link";
import { ApiError } from "@/shared/api/client";
import { useAuth } from "@/features/auth/context/AuthProvider";
import { useTranslation } from "@/features/i18n/context/I18nProvider";

export function LoginForm() {
  const { login } = useAuth();
  const { t } = useTranslation();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      await login(email, password);
    } catch (caught) {
      if (caught instanceof ApiError) {
        setError(caught.message);
      } else {
        setError(t("auth.loginFailed"));
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="auth-form" onSubmit={handleSubmit}>
      <h1>{t("auth.loginTitle")}</h1>
      <p className="muted">{t("auth.loginSubtitle")}</p>
      {error ? <p className="error">{error}</p> : null}
      <label>
        {t("common.email")}
        <input
          type="email"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          required
          autoComplete="email"
        />
      </label>
      <label>
        {t("common.password")}
        <input
          type="password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          required
          autoComplete="current-password"
        />
      </label>
      <button type="submit" disabled={isSubmitting}>
        {isSubmitting ? t("auth.signingIn") : t("auth.signIn")}
      </button>
      <p className="muted">
        {t("auth.noAccount")} <Link href="/register">{t("auth.registerLink")}</Link>
      </p>
    </form>
  );
}
