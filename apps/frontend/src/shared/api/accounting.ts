import { apiRequest } from "@/shared/api/client";
import type { Account, Category, CategoryType, Transaction } from "@/shared/types/api";

export function fetchAccounts(organizationId: string): Promise<Account[]> {
  return apiRequest<Account[]>(`/api/v1/organizations/${organizationId}/accounts`);
}

export function createAccount(organizationId: string, name: string, currency: string): Promise<Account> {
  return apiRequest<Account>(`/api/v1/organizations/${organizationId}/accounts`, {
    method: "POST",
    body: { name, currency }
  });
}

export function deleteAccount(organizationId: string, accountId: string): Promise<void> {
  return apiRequest<void>(`/api/v1/organizations/${organizationId}/accounts/${accountId}`, {
    method: "DELETE"
  });
}

export function fetchCategories(organizationId: string): Promise<Category[]> {
  return apiRequest<Category[]>(`/api/v1/organizations/${organizationId}/categories`);
}

export function createCategory(
  organizationId: string,
  name: string,
  type: CategoryType
): Promise<Category> {
  return apiRequest<Category>(`/api/v1/organizations/${organizationId}/categories`, {
    method: "POST",
    body: { name, type }
  });
}

export function deleteCategory(organizationId: string, categoryId: string): Promise<void> {
  return apiRequest<void>(`/api/v1/organizations/${organizationId}/categories/${categoryId}`, {
    method: "DELETE"
  });
}

function buildTransactionQuery(filters: {
  from?: string;
  to?: string;
  accountId?: string;
  categoryId?: string;
}): string {
  const query = new URLSearchParams();
  if (filters.from) {
    query.set("from", filters.from);
  }
  if (filters.to) {
    query.set("to", filters.to);
  }
  if (filters.accountId) {
    query.set("accountId", filters.accountId);
  }
  if (filters.categoryId) {
    query.set("categoryId", filters.categoryId);
  }
  return query.size > 0 ? `?${query.toString()}` : "";
}

export function fetchTransactions(
  organizationId: string,
  filters: {
    from?: string;
    to?: string;
    accountId?: string;
    categoryId?: string;
  } = {}
): Promise<Transaction[]> {
  return apiRequest<Transaction[]>(
    `/api/v1/organizations/${organizationId}/transactions${buildTransactionQuery(filters)}`
  );
}

export function createTransaction(
  organizationId: string,
  payload: {
    accountId: string;
    categoryId: string;
    amountCents: number;
    currency: string;
    bookedAt?: string;
    description?: string;
  }
): Promise<Transaction> {
  return apiRequest<Transaction>(`/api/v1/organizations/${organizationId}/transactions`, {
    method: "POST",
    body: payload
  });
}

export function deleteTransaction(organizationId: string, transactionId: string): Promise<void> {
  return apiRequest<void>(`/api/v1/organizations/${organizationId}/transactions/${transactionId}`, {
    method: "DELETE"
  });
}

export function updateTransaction(
  organizationId: string,
  transactionId: string,
  payload: {
    accountId: string;
    categoryId: string;
    amountCents: number;
    bookedAt: string;
    description?: string;
  }
): Promise<Transaction> {
  return apiRequest<Transaction>(
    `/api/v1/organizations/${organizationId}/transactions/${transactionId}`,
    {
      method: "PUT",
      body: payload
    }
  );
}
