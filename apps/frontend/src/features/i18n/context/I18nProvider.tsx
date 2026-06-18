"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useSyncExternalStore,
  type ReactNode
} from "react";
import { messagesByLocale } from "@/shared/i18n/locales";
import { translate } from "@/shared/i18n/translate";
import type { Locale } from "@/shared/i18n/types";
import {
  getLocaleSnapshot,
  getServerLocaleSnapshot,
  subscribeLocale,
  writeLocale
} from "@/features/i18n/context/localeStore";

type I18nContextValue = {
  locale: Locale;
  setLocale: (locale: Locale) => void;
  t: (key: string, params?: Record<string, string>) => string;
};

const I18nContext = createContext<I18nContextValue | null>(null);

type I18nProviderProps = Readonly<{
  children: ReactNode;
}>;

export function I18nProvider({ children }: I18nProviderProps) {
  const locale = useSyncExternalStore(subscribeLocale, getLocaleSnapshot, getServerLocaleSnapshot);

  useEffect(() => {
    document.documentElement.lang = locale;
  }, [locale]);

  const setLocale = useCallback((nextLocale: Locale) => {
    writeLocale(nextLocale);
  }, []);

  const t = useCallback(
    (key: string, params?: Record<string, string>) =>
      translate(messagesByLocale[locale], key, params),
    [locale]
  );

  const value = useMemo(() => ({ locale, setLocale, t }), [locale, setLocale, t]);

  return <I18nContext.Provider value={value}>{children}</I18nContext.Provider>;
}

export function useI18n(): I18nContextValue {
  const context = useContext(I18nContext);
  if (!context) {
    throw new Error("useI18n must be used within I18nProvider.");
  }
  return context;
}

export function useTranslation() {
  const { locale, setLocale, t } = useI18n();
  return { locale, setLocale, t };
}
