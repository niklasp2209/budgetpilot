"use client";

import { useEffect, useState } from "react";
import { ApiError } from "@/shared/api/client";
import {
  fetchBudgetVsActual,
  fetchBudgets,
  fetchByCategory,
  fetchCashflow
} from "@/shared/api/budgets";
import { formatCents } from "@/shared/lib/format";
import { useOrganization } from "@/features/organization/context/OrganizationProvider";
import type {
  BudgetVsActualReport,
  CashflowReport,
  CategoryAmount
} from "@/shared/types/api";

export function DashboardView() {
  const { selectedOrganization } = useOrganization();
  const [cashflow, setCashflow] = useState<CashflowReport | null>(null);
  const [categories, setCategories] = useState<CategoryAmount[]>([]);
  const [budgetVsActual, setBudgetVsActual] = useState<BudgetVsActualReport | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const organizationId = selectedOrganization?.id;
    if (!organizationId) {
      return;
    }

    const orgId = organizationId;
    let cancelled = false;

    async function loadDashboard() {
      setIsLoading(true);
      setError(null);
      try {
        const [cashflowReport, categoryReport, budgets] = await Promise.all([
          fetchCashflow(orgId),
          fetchByCategory(orgId),
          fetchBudgets(orgId)
        ]);

        let budgetReport: BudgetVsActualReport | null = null;
        if (budgets.length > 0) {
          const currentMonth = new Date().toISOString().slice(0, 7);
          const matchingBudget =
            budgets.find((budget) => budget.periodStart.startsWith(currentMonth)) ?? budgets[0];
          budgetReport = await fetchBudgetVsActual(orgId, matchingBudget.id);
        }

        if (!cancelled) {
          setCashflow(cashflowReport);
          setCategories(categoryReport);
          setBudgetVsActual(budgetReport);
        }
      } catch (caught) {
        if (!cancelled) {
          if (caught instanceof ApiError) {
            setError(caught.message);
          } else {
            setError("Failed to load dashboard.");
          }
        }
      } finally {
        if (!cancelled) {
          setIsLoading(false);
        }
      }
    }

    void loadDashboard();

    return () => {
      cancelled = true;
    };
  }, [selectedOrganization]);

  if (!selectedOrganization) {
    return null;
  }

  if (isLoading) {
    return <p className="muted">Loading dashboard...</p>;
  }

  if (error) {
    return <p className="error">{error}</p>;
  }

  return (
    <div className="dashboard-grid">
      <section className="card">
        <h2>Cashflow (last 30 days)</h2>
        {cashflow ? (
          <dl className="metric-list">
            <div>
              <dt>Income</dt>
              <dd className="positive">{formatCents(cashflow.incomeCents)}</dd>
            </div>
            <div>
              <dt>Expenses</dt>
              <dd className="negative">{formatCents(cashflow.expenseCents)}</dd>
            </div>
            <div>
              <dt>Net</dt>
              <dd>{formatCents(cashflow.netCents)}</dd>
            </div>
          </dl>
        ) : (
          <p className="muted">No cashflow data.</p>
        )}
      </section>

      <section className="card">
        <h2>Expenses by category</h2>
        {categories.length === 0 ? (
          <p className="muted">No expenses in this period.</p>
        ) : (
          <ul className="data-list">
            {categories.map((category) => (
              <li key={category.categoryId}>
                <span>{category.categoryName}</span>
                <span>{formatCents(category.amountCents)}</span>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="card card-wide">
        <h2>Budget vs. actual</h2>
        {!budgetVsActual || budgetVsActual.items.length === 0 ? (
          <p className="muted">No budget data for the current month.</p>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Category</th>
                <th>Budget</th>
                <th>Actual</th>
                <th>Delta</th>
              </tr>
            </thead>
            <tbody>
              {budgetVsActual.items.map((item) => {
                const delta = item.budgetCents - item.actualCents;
                return (
                  <tr key={item.categoryId}>
                    <td>{item.categoryName}</td>
                    <td>{formatCents(item.budgetCents)}</td>
                    <td>{formatCents(item.actualCents)}</td>
                    <td className={delta >= 0 ? "positive" : "negative"}>{formatCents(delta)}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </section>
    </div>
  );
}
