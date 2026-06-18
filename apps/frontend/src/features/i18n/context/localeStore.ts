import { DEFAULT_LOCALE, isLocale } from "@/shared/i18n/config";
import type { Locale } from "@/shared/i18n/types";
import { getStoredLocale, setStoredLocale } from "@/shared/lib/storage";

const listeners = new Set<() => void>();

function emitLocaleChange() {
  listeners.forEach((listener) => listener());
}

export function subscribeLocale(listener: () => void): () => void {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

export function getServerLocaleSnapshot(): Locale {
  return DEFAULT_LOCALE;
}

export function getLocaleSnapshot(): Locale {
  const stored = getStoredLocale();
  if (stored && isLocale(stored)) {
    return stored;
  }
  return DEFAULT_LOCALE;
}

export function writeLocale(locale: Locale): void {
  setStoredLocale(locale);
  emitLocaleChange();
}
