import type { Locale } from "@/shared/i18n/types";
import { localeToIntl } from "@/shared/i18n/config";

export function formatCents(cents: number, currency = "EUR", locale: Locale = "en"): string {
  return new Intl.NumberFormat(localeToIntl(locale), {
    style: "currency",
    currency
  }).format(cents / 100);
}

export function slugify(value: string): string {
  return value
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "")
    .slice(0, 255);
}
