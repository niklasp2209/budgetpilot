import type { Locale, Messages } from "@/shared/i18n/types";
import { de } from "@/shared/i18n/locales/de";
import { en } from "@/shared/i18n/locales/en";

export const messagesByLocale: Record<Locale, Messages> = {
  en,
  de
};
