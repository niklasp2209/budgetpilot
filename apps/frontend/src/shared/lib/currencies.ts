export const ORGANIZATION_CURRENCY_CODES = [
  "EUR",
  "USD",
  "GBP",
  "CHF",
  "JPY",
  "CAD",
  "AUD",
  "SEK",
  "NOK",
  "DKK",
  "PLN",
  "CZK"
] as const;

export type OrganizationCurrencyCode = (typeof ORGANIZATION_CURRENCY_CODES)[number];

export const DEFAULT_ORGANIZATION_CURRENCY: OrganizationCurrencyCode = "EUR";

export const CURRENCY_SYMBOLS: Record<OrganizationCurrencyCode, string> = {
  EUR: "€",
  USD: "$",
  GBP: "£",
  CHF: "CHF",
  JPY: "¥",
  CAD: "CA$",
  AUD: "A$",
  SEK: "kr",
  NOK: "kr",
  DKK: "kr",
  PLN: "zł",
  CZK: "Kč"
};

export function getCurrencySymbol(currency: string): string {
  return CURRENCY_SYMBOLS[currency as OrganizationCurrencyCode] ?? currency;
}

export function formatCurrencyOption(currency: string, label: string): string {
  return `${getCurrencySymbol(currency)} ${label} (${currency})`;
}
