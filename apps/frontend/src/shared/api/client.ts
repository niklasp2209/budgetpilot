import type { ApiErrorBody, AuthTokens } from "@/shared/types/api";
import {
  clearStoredTokens,
  getStoredTokens,
  setStoredTokens,
  type StoredTokens
} from "@/shared/lib/storage";

export class ApiError extends Error {
  readonly status: number;
  readonly code: string;

  constructor(status: number, code: string, message: string) {
    super(message);
    this.status = status;
    this.code = code;
  }
}

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

let refreshPromise: Promise<StoredTokens | null> | null = null;

async function refreshTokens(): Promise<StoredTokens | null> {
  const stored = getStoredTokens();
  if (!stored) {
    return null;
  }

  const response = await fetch(`${API_BASE_URL}/api/v1/auth/refresh`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken: stored.refreshToken })
  });

  if (!response.ok) {
    clearStoredTokens();
    return null;
  }

  const tokens = (await response.json()) as AuthTokens;
  const nextTokens: StoredTokens = {
    accessToken: tokens.accessToken,
    refreshToken: tokens.refreshToken
  };
  setStoredTokens(nextTokens);
  return nextTokens;
}

async function ensureRefreshedTokens(): Promise<StoredTokens | null> {
  if (!refreshPromise) {
    refreshPromise = refreshTokens().finally(() => {
      refreshPromise = null;
    });
  }
  return refreshPromise;
}

type RequestOptions = Omit<RequestInit, "body"> & {
  body?: unknown;
  skipAuth?: boolean;
};

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const headers = new Headers(options.headers);
  if (options.body !== undefined) {
    headers.set("Content-Type", "application/json");
  }

  if (!options.skipAuth) {
    const stored = getStoredTokens();
    if (!stored) {
      throw new ApiError(401, "UNAUTHORIZED", "Not authenticated.");
    }
    headers.set("Authorization", `Bearer ${stored.accessToken}`);
  }

  const execute = () =>
    fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers,
      body: options.body !== undefined ? JSON.stringify(options.body) : undefined
    });

  let response = await execute();

  if (response.status === 401 && !options.skipAuth) {
    const refreshed = await ensureRefreshedTokens();
    if (!refreshed) {
      throw new ApiError(401, "UNAUTHORIZED", "Session expired.");
    }
    headers.set("Authorization", `Bearer ${refreshed.accessToken}`);
    response = await execute();
  }

  if (response.status === 204) {
    return undefined as T;
  }

  if (!response.ok) {
    let code = "REQUEST_FAILED";
    let message = "Request failed.";
    try {
      const errorBody = (await response.json()) as ApiErrorBody;
      code = errorBody.code;
      message = errorBody.message;
    } catch {
      // ignore parse errors
    }
    throw new ApiError(response.status, code, message);
  }

  return (await response.json()) as T;
}

export async function loginRequest(email: string, password: string): Promise<AuthTokens> {
  return apiRequest<AuthTokens>("/api/v1/auth/login", {
    method: "POST",
    skipAuth: true,
    body: { email, password }
  });
}

export async function registerRequest(email: string, password: string): Promise<AuthTokens> {
  return apiRequest<AuthTokens>("/api/v1/auth/register", {
    method: "POST",
    skipAuth: true,
    body: { email, password }
  });
}
