"use client";

import { useEffect, useMemo, useState } from "react";
import { ApiError } from "@/shared/api/client";
import { fetchAccounts } from "@/shared/api/accounting";
import {
  fetchBudgetVsActual,
  fetchBudgets,
  fetchByCategory,
  fetchCashflow
} from "@/shared/api/budgets";
import { BarChart } from "@/shared/components/BarChart";
import { DateRangeFilter } from "@/shared/components/DateRangeFilter";
import { EmptyState } from "@/shared/components/EmptyState";
import { formatCents } from "@/shared/lib/format";
import {
  defaultCustomFromDate,
  defaultCustomToDate,
  formatDateRangeLabel,
  resolveDateRangeIso,
  type DateRangePreset
} from "@/shared/lib/dateRange";
import { runInEffectAsync } from "@/shared/lib/runInEffectAsync";
import { useOrganization } from "@/features/organization/context/OrganizationProvider";
import type { BudgetVsActualReport, CashflowReport, CategoryAmount } from "@/shared/types/api";

export function DashboardView() {
  const { selectedOrganization } = useOrganization();
  const [cashflow, setCashflow] = useState<CashflowReport | null>(null);
  const [categories, setCategories] = useState<CategoryAmount[]>([]);
  const [budgetVsActual, setBudgetVsActual] = useState<BudgetVsActualReport | null>(null);
  const [hasAccounts, setHasAccounts] = useState(true);
  const [hasBudgets, setHasBudgets] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const [preset, setPreset] = useState<DateRangePreset>("30d");
  const [customFrom, setCustomFrom] = useState(defaultCustomFromDate);
  const [customTo, setCustomTo] = useState(defaultCustomToDate);

  const range = useMemo(
    () => resolveDateRangeIso(preset, customFrom, customTo),
    [preset, customFrom, customTo]
  );

  useEffect(() => {
    const organizationId = selectedOrganization?.id;
    if (!organizationId) {
      return;
    }

    const orgId = organizationId;

    return runInEffectAsync(async (isCancelled) => {
      setIsLoading(true);
      setError(null);
      try {
        const [accounts, cashflowReport, categoryReport, budgets] = await Promise.all([
          fetchAccounts(orgId),
          fetchCashflow(orgId, range),
          fetchByCategory(orgId, range),
          fetchBudgets(orgId)
        ]);

        let budgetReport: BudgetVsActualReport | null = null;
        if (budgets.length > 0) {
          const currentMonth = new Date().toISOString().slice(0, 7);
          const matchingBudget =
            budgets.find((budget) => budget.periodStart.startsWith(currentMonth)) ?? budgets[0];
          budgetReport = await fetchBudgetVsActual(orgId, matchingBudget.id);
        }

        if (!isCancelled()) {
          setHasAccounts(accounts.length > 0);
          setHasBudgets(budgets.length > 0);
          setCashflow(cashflowReport);
          setCategories(categoryReport);
          setBudgetVsActual(budgetReport);
        }
      } catch (caught) {
        if (!isCancelled()) {
          setError(caught instanceof ApiError ? caught.message : "Failed to load dashboard.");
        }
      } finally {
        if (!isCancelled()) {
          setIsLoading(false);
        }
      }
    });
  }, [selectedOrganization?.id, range]);

  if (!selectedOrganization) {
    return null;
  }

  if (isLoading) {
    return <p className="muted">Loading dashboard...</p>;
  }

  if (error) {
    return <p className="error">{error}</p>;
  }

  if (!hasAccounts) {
    return (
      <EmptyState
        title="Set up accounting first"
        description="Create at least one account and record transactions before the dashboard can show reports."
        href="/accounting"
        linkLabel="Go to Accounting"
      />
    );
  }

  const periodLabel = formatDateRangeLabel(preset);
  const hasCashflowData =
    cashflow != null && (cashflow.incomeCents > 0 || cashflow.expenseCents > 0);

  return (
    <div className="stack">
      <div className="filter-bar">
        <span className="muted">Period</span>
        <DateRangeFilter
          preset={preset}
          customFrom={customFrom}
          customTo={customTo}
          onPresetChange={setPreset}
          onCustomFromChange={setCustomFrom}
          onCustomToChange={setCustomTo}
        />
      </div>

      <div className="dashboard-grid">
        <section className="card">
          <h2>Cashflow ({periodLabel})</h2>
          {!hasCashflowData ? (
            <EmptyState
              title="No transactions in this period"
              description="Add income or expense transactions in Accounting to see cashflow here."
              href="/accounting"
              linkLabel="Add transactions"
            />
          ) : (
            <>
              <BarChart
                items={[
                  { label: "Income", value: cashflow.incomeCents, tone: "positive" },
                  { label: "Expenses", value: cashflow.expenseCents, tone: "negative" }
                ]}
              />
              <dl className="metric-list">
                <div>
                  <dt>Net</dt>
                  <dd className={cashflow.netCents >= 0 ? "positive" : "negative"}>
                    {formatCents(cashflow.netCents)}
                  </dd>
                </div>
              </dl>
            </>
          )}
        </section>

        <section className="card">
          <h2>Expenses by category ({periodLabel})</h2>
          {categories.length === 0 ? (
            <EmptyState
              title="No expenses in this period"
              description="Record expense transactions with categories to see a breakdown here."
              href="/accounting"
              linkLabel="Go to Accounting"
            />
          ) : (
            <BarChart
              items={categories.map((category) => ({
                label: category.categoryName,
                value: category.amountCents,
                tone: "negative" as const
              }))}
            />
          )}
        </section>

        <section className="card card-wide">
          <h2>Budget vs. actual</h2>
          {!hasBudgets ? (
            <EmptyState
              title="No budgets yet"
              description="Create a monthly budget to compare planned and actual spending."
              href="/budgets"
              linkLabel="Go to Budgets"
            />
          ) : !budgetVsActual || budgetVsActual.items.length === 0 ? (
            <EmptyState
              title="No budget items for this month"
              description="Add budget items for the current month to see budget versus actual."
              href="/budgets"
              linkLabel="Manage budgets"
            />
          ) : (
            <>
              <BarChart
                items={budgetVsActual.items.map((item) => ({
                  label: item.categoryName,
                  value: item.actualCents,
                  tone: item.actualCents > item.budgetCents ? "negative" : "neutral"
                }))}
              />
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
            </>
          )}
        </section>
      </div>
    </div>
  );
}
