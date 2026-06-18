import type { Locale, LocaleDefinition } from "@/shared/i18n/types";

export const DEFAULT_LOCALE: Locale = "en";

export const SUPPORTED_LOCALES: LocaleDefinition[] = [
  { id: "en", flag: "🇬🇧", label: "English" },
  { id: "de", flag: "🇩🇪", label: "Deutsch" }
];

export function isLocale(value: string): value is Locale {
  return SUPPORTED_LOCALES.some((locale) => locale.id === value);
}

export function localeToIntl(locale: Locale): string {
  return locale === "de" ? "de-DE" : "en-US";
}
