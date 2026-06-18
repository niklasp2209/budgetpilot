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

export function fetchCashflow(organizationId: string): Promise<CashflowReport> {
  return apiRequest<CashflowReport>(`/api/v1/organizations/${organizationId}/reports/cashflow`);
}

export function fetchByCategory(organizationId: string): Promise<CategoryAmount[]> {
  return apiRequest<CategoryAmount[]>(`/api/v1/organizations/${organizationId}/reports/by-category`);
}

export function fetchBudgetVsActual(
  organizationId: string,
  budgetId: string
): Promise<BudgetVsActualReport> {
  return apiRequest<BudgetVsActualReport>(
    `/api/v1/organizations/${organizationId}/reports/budget-vs-actual?budgetId=${budgetId}`
  );
}
