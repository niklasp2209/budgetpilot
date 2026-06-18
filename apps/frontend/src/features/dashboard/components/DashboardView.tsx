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
import { useTranslation } from "@/features/i18n/context/I18nProvider";
import { useOrganization } from "@/features/organization/context/OrganizationProvider";
import type { BudgetVsActualReport, CashflowReport, CategoryAmount } from "@/shared/types/api";

export function DashboardView() {
  const { selectedOrganization } = useOrganization();
  const { t, locale } = useTranslation();
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
          setError(caught instanceof ApiError ? caught.message : t("dashboard.loadFailed"));
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
    return <p className="muted">{t("dashboard.loading")}</p>;
  }

  if (error) {
    return <p className="error">{error}</p>;
  }

  if (!hasAccounts) {
    return (
      <EmptyState
        title={t("dashboard.setupAccountingTitle")}
        description={t("dashboard.setupAccountingDescription")}
        href="/accounting"
        linkLabel={t("dashboard.goToAccounting")}
      />
    );
  }

  const periodLabel = formatDateRangeLabel(preset, locale);
  const hasCashflowData =
    cashflow != null && (cashflow.incomeCents > 0 || cashflow.expenseCents > 0);

  return (
    <div className="stack">
      <div className="filter-bar">
        <span className="muted">{t("common.period")}</span>
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
          <h2>{t("dashboard.cashflow", { period: periodLabel })}</h2>
          {!hasCashflowData ? (
            <EmptyState
              title={t("dashboard.noTransactionsTitle")}
              description={t("dashboard.noTransactionsDescription")}
              href="/accounting"
              linkLabel={t("dashboard.addTransactions")}
            />
          ) : (
            <>
              <BarChart
                items={[
                  { label: t("dashboard.income"), value: cashflow.incomeCents, tone: "positive" },
                  { label: t("dashboard.expenses"), value: cashflow.expenseCents, tone: "negative" }
                ]}
              />
              <dl className="metric-list">
                <div>
                  <dt>{t("dashboard.net")}</dt>
                  <dd className={cashflow.netCents >= 0 ? "positive" : "negative"}>
                    {formatCents(cashflow.netCents, "EUR", locale)}
                  </dd>
                </div>
              </dl>
            </>
          )}
        </section>

        <section className="card">
          <h2>{t("dashboard.expensesByCategory", { period: periodLabel })}</h2>
          {categories.length === 0 ? (
            <EmptyState
              title={t("dashboard.noExpensesTitle")}
              description={t("dashboard.noExpensesDescription")}
              href="/accounting"
              linkLabel={t("dashboard.goToAccounting")}
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
          <h2>{t("dashboard.budgetVsActual")}</h2>
          {!hasBudgets ? (
            <EmptyState
              title={t("dashboard.noBudgetsTitle")}
              description={t("dashboard.noBudgetsDescription")}
              href="/budgets"
              linkLabel={t("dashboard.goToBudgets")}
            />
          ) : !budgetVsActual || budgetVsActual.items.length === 0 ? (
            <EmptyState
              title={t("dashboard.noBudgetItemsTitle")}
              description={t("dashboard.noBudgetItemsDescription")}
              href="/budgets"
              linkLabel={t("dashboard.manageBudgets")}
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
                    <th>{t("dashboard.tableCategory")}</th>
                    <th>{t("dashboard.tableBudget")}</th>
                    <th>{t("dashboard.tableActual")}</th>
                    <th>{t("dashboard.tableDelta")}</th>
                  </tr>
                </thead>
                <tbody>
                  {budgetVsActual.items.map((item) => {
                    const delta = item.budgetCents - item.actualCents;
                    return (
                      <tr key={item.categoryId}>
                        <td>{item.categoryName}</td>
                        <td>{formatCents(item.budgetCents, "EUR", locale)}</td>
                        <td>{formatCents(item.actualCents, "EUR", locale)}</td>
                        <td className={delta >= 0 ? "positive" : "negative"}>
                          {formatCents(delta, "EUR", locale)}
                        </td>
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
