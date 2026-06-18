import type { Locale } from "@/shared/i18n/types";
import { localeToIntl } from "@/shared/i18n/config";

export type DateRangePreset = "30d" | "90d" | "365d" | "all" | "custom";

export function defaultCustomFromDate(): string {
  const date = new Date();
  date.setDate(date.getDate() - 30);
  return date.toISOString().slice(0, 10);
}

export function defaultCustomToDate(): string {
  return new Date().toISOString().slice(0, 10);
}

export function resolveDateRangeIso(
  preset: DateRangePreset,
  customFrom: string,
  customTo: string
): { from: string; to: string } {
  const to = new Date();
  const from = new Date();

  if (preset === "30d") {
    from.setDate(from.getDate() - 30);
  } else if (preset === "90d") {
    from.setDate(from.getDate() - 90);
  } else if (preset === "365d") {
    from.setDate(from.getDate() - 365);
  } else if (preset === "all") {
    from.setFullYear(from.getFullYear() - 10);
  } else {
    from.setTime(new Date(`${customFrom}T00:00:00`).getTime());
    to.setTime(new Date(`${customTo}T23:59:59`).getTime());
  }

  if (preset !== "custom") {
    from.setHours(0, 0, 0, 0);
    to.setHours(23, 59, 59, 999);
  }

  return { from: from.toISOString(), to: to.toISOString() };
}

export function toLocalDateTimeInput(iso: string): string {
  const date = new Date(iso);
  const pad = (value: number) => String(value).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

export function formatDateRangeLabel(preset: DateRangePreset, locale: Locale = "en"): string {
  const labels =
    locale === "de"
      ? {
          "30d": "Letzte 30 Tage",
          "90d": "Letzte 90 Tage",
          "365d": "Letzte 12 Monate",
          all: "Gesamt",
          custom: "Eigener Zeitraum"
        }
      : {
          "30d": "Last 30 days",
          "90d": "Last 90 days",
          "365d": "Last 12 months",
          all: "All time",
          custom: "Custom range"
        };
  return labels[preset];
}

export function formatDateTime(iso: string, locale: Locale = "en"): string {
  return new Date(iso).toLocaleString(localeToIntl(locale));
}
