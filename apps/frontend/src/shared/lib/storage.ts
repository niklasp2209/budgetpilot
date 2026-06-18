const ACCESS_TOKEN_KEY = "budgetpilot.accessToken";
const REFRESH_TOKEN_KEY = "budgetpilot.refreshToken";
const ORGANIZATION_ID_KEY = "budgetpilot.organizationId";
const LOCALE_KEY = "budgetpilot.locale";

export type StoredTokens = {
  accessToken: string;
  refreshToken: string;
};

export function getStoredTokens(): StoredTokens | null {
  if (typeof window === "undefined") {
    return null;
  }
  const accessToken = window.localStorage.getItem(ACCESS_TOKEN_KEY);
  const refreshToken = window.localStorage.getItem(REFRESH_TOKEN_KEY);
  if (!accessToken || !refreshToken) {
    return null;
  }
  return { accessToken, refreshToken };
}

export function setStoredTokens(tokens: StoredTokens): void {
  window.localStorage.setItem(ACCESS_TOKEN_KEY, tokens.accessToken);
  window.localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken);
}

export function clearStoredTokens(): void {
  window.localStorage.removeItem(ACCESS_TOKEN_KEY);
  window.localStorage.removeItem(REFRESH_TOKEN_KEY);
}

export function getStoredOrganizationId(): string | null {
  if (typeof window === "undefined") {
    return null;
  }
  return window.localStorage.getItem(ORGANIZATION_ID_KEY);
}

export function setStoredOrganizationId(organizationId: string): void {
  window.localStorage.setItem(ORGANIZATION_ID_KEY, organizationId);
}

export function clearStoredOrganizationId(): void {
  window.localStorage.removeItem(ORGANIZATION_ID_KEY);
}

export function getStoredLocale(): string | null {
  if (typeof window === "undefined") {
    return null;
  }
  return window.localStorage.getItem(LOCALE_KEY);
}

export function setStoredLocale(locale: string): void {
  window.localStorage.setItem(LOCALE_KEY, locale);
}
