import { apiRequest } from "@/shared/api/client";
import type {
  Budget,
  BudgetItem,
  BudgetSummary,
  BudgetVsActualReport,
  CashflowReport,
  CategoryAmount
} from "@/shared/types/api";

export function fetchBudgets(organizationId: string): Promise<Budget[]> {
  return apiRequest<Budget[]>(`/api/v1/organizations/${organizationId}/budgets`);
}

export function createBudget(
  organizationId: string,
  payload: { name: string; periodStart: string; currency: string }
): Promise<Budget> {
  return apiRequest<Budget>(`/api/v1/organizations/${organizationId}/budgets`, {
    method: "POST",
    body: payload
  });
}

export function upsertBudgetItem(
  organizationId: string,
  budgetId: string,
  categoryId: string,
  amountCents: number
): Promise<BudgetItem> {
  return apiRequest<BudgetItem>(`/api/v1/organizations/${organizationId}/budgets/${budgetId}/items`, {
    method: "PUT",
    body: { categoryId, amountCents }
  });
}

export function fetchBudgetSummary(organizationId: string, budgetId: string): Promise<BudgetSummary> {
  return apiRequest<BudgetSummary>(
    `/api/v1/organizations/${organizationId}/budgets/${budgetId}/summary`
  );
}

export function fetchCashflow(
  organizationId: string,
  range?: { from: string; to: string }
): Promise<CashflowReport> {
  const query = new URLSearchParams();
  if (range) {
    query.set("from", range.from);
    query.set("to", range.to);
  }
  const suffix = query.size > 0 ? `?${query.toString()}` : "";
  return apiRequest<CashflowReport>(
    `/api/v1/organizations/${organizationId}/reports/cashflow${suffix}`
  );
}

export function fetchByCategory(
  organizationId: string,
  range?: { from: string; to: string }
): Promise<CategoryAmount[]> {
  const query = new URLSearchParams();
  if (range) {
    query.set("from", range.from);
    query.set("to", range.to);
  }
  const suffix = query.size > 0 ? `?${query.toString()}` : "";
  return apiRequest<CategoryAmount[]>(
    `/api/v1/organizations/${organizationId}/reports/by-category${suffix}`
  );
}

export function fetchBudgetVsActual(
  organizationId: string,
  budgetId: string
): Promise<BudgetVsActualReport> {
  return apiRequest<BudgetVsActualReport>(
    `/api/v1/organizations/${organizationId}/reports/budget-vs-actual?budgetId=${budgetId}`
  );
}
