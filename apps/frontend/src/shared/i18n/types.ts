import type { en } from "@/shared/i18n/locales/en";

type DeepStringRecord<T> = {
  [K in keyof T]: T[K] extends string ? string : DeepStringRecord<T[K]>;
};

export type Messages = DeepStringRecord<typeof en>;

export type Locale = "en" | "de";

export type LocaleDefinition = {
  id: Locale;
  flag: string;
  label: string;
};
