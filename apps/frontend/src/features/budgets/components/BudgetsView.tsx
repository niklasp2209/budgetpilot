"use client";

import { FormEvent, useEffect, useState } from "react";
import { ApiError } from "@/shared/api/client";
import {
  createBudget,
  fetchBudgetSummary,
  fetchBudgets,
  upsertBudgetItem
} from "@/shared/api/budgets";
import { fetchCategories } from "@/shared/api/accounting";
import { formatCents } from "@/shared/lib/format";
import { hasPermission } from "@/shared/lib/permissions";
import { runInEffectAsync } from "@/shared/lib/runInEffectAsync";
import { useTranslation } from "@/features/i18n/context/I18nProvider";
import { useOrganization } from "@/features/organization/context/OrganizationProvider";
import type { Budget, BudgetSummary, Category } from "@/shared/types/api";

export function BudgetsView() {
  const { selectedOrganization } = useOrganization();
  const { t, locale } = useTranslation();
  const [budgets, setBudgets] = useState<Budget[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [selectedBudgetId, setSelectedBudgetId] = useState<string>("");
  const [summary, setSummary] = useState<BudgetSummary | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const canWrite = hasPermission(selectedOrganization, "BUDGET_WRITE");

  const [budgetName, setBudgetName] = useState("");
  const [budgetMonth, setBudgetMonth] = useState(() => new Date().toISOString().slice(0, 7));
  const [itemCategoryId, setItemCategoryId] = useState("");
  const [itemAmount, setItemAmount] = useState("");

  async function loadBudgets() {
    if (!selectedOrganization) {
      return;
    }
    setIsLoading(true);
    setError(null);
    try {
      const [loadedBudgets, loadedCategories] = await Promise.all([
        fetchBudgets(selectedOrganization.id),
        fetchCategories(selectedOrganization.id)
      ]);
      setBudgets(loadedBudgets);
      setCategories(loadedCategories.filter((category) => category.type !== "TRANSFER"));

      const nextBudgetId =
        loadedBudgets.find((budget) => budget.id === selectedBudgetId)?.id
        ?? loadedBudgets[0]?.id
        ?? "";
      setSelectedBudgetId(nextBudgetId);

      if (loadedCategories.length > 0 && !itemCategoryId) {
        setItemCategoryId(loadedCategories[0].id);
      }
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : t("budgets.loadFailed"));
    } finally {
      setIsLoading(false);
    }
  }

  async function loadSummary(budgetId: string) {
    if (!selectedOrganization || !budgetId) {
      setSummary(null);
      return;
    }
    try {
      const loadedSummary = await fetchBudgetSummary(selectedOrganization.id, budgetId);
      setSummary(loadedSummary);
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : t("budgets.summaryFailed"));
    }
  }

  useEffect(() => {
    const organizationId = selectedOrganization?.id;
    if (!organizationId) {
      return;
    }

    return runInEffectAsync(async (isCancelled) => {
      setIsLoading(true);
      setError(null);
      try {
        const [loadedBudgets, loadedCategories] = await Promise.all([
          fetchBudgets(organizationId),
          fetchCategories(organizationId)
        ]);
        if (isCancelled()) {
          return;
        }
        setBudgets(loadedBudgets);
        setCategories(loadedCategories.filter((category) => category.type !== "TRANSFER"));

        setSelectedBudgetId((current) =>
          loadedBudgets.find((budget) => budget.id === current)?.id
          ?? loadedBudgets[0]?.id
          ?? ""
        );

        if (loadedCategories.length > 0) {
          setItemCategoryId((current) => current || loadedCategories[0].id);
        }
      } catch (caught) {
        if (!isCancelled()) {
          setError(caught instanceof ApiError ? caught.message : t("budgets.loadFailed"));
        }
      } finally {
        if (!isCancelled()) {
          setIsLoading(false);
        }
      }
    });
  }, [selectedOrganization?.id]);

  useEffect(() => {
    const organizationId = selectedOrganization?.id;
    if (!organizationId) {
      return;
    }

    if (!selectedBudgetId) {
      return runInEffectAsync(async (isCancelled) => {
        if (!isCancelled()) {
          setSummary(null);
        }
      });
    }

    return runInEffectAsync(async (isCancelled) => {
      try {
        const loadedSummary = await fetchBudgetSummary(organizationId, selectedBudgetId);
        if (!isCancelled()) {
          setSummary(loadedSummary);
        }
      } catch (caught) {
        if (!isCancelled()) {
          setError(caught instanceof ApiError ? caught.message : t("budgets.summaryFailed"));
        }
      }
    });
  }, [selectedOrganization?.id, selectedBudgetId]);

  async function handleCreateBudget(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedOrganization) {
      return;
    }
    setError(null);
    try {
      const created = await createBudget(selectedOrganization.id, {
        name: budgetName.trim(),
        periodStart: `${budgetMonth}-01`,
        currency: "EUR"
      });
      setBudgetName("");
      setSelectedBudgetId(created.id);
      await loadBudgets();
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : t("budgets.createFailed"));
    }
  }

  async function handleUpsertItem(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedOrganization || !selectedBudgetId) {
      return;
    }
    const amountCents = Math.round(Number.parseFloat(itemAmount) * 100);
    if (!Number.isFinite(amountCents) || amountCents < 0) {
      setError(t("budgets.amountNonNegative"));
      return;
    }
    setError(null);
    try {
      await upsertBudgetItem(selectedOrganization.id, selectedBudgetId, itemCategoryId, amountCents);
      setItemAmount("");
      await loadSummary(selectedBudgetId);
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : t("budgets.saveItemFailed"));
    }
  }

  if (!selectedOrganization) {
    return null;
  }

  if (isLoading) {
    return <p className="muted">{t("budgets.loading")}</p>;
  }

  return (
    <div className="stack">
      {error ? <p className="error">{error}</p> : null}

      {canWrite ? (
        <section className="card card-wide">
          <h2>{t("budgets.createBudget")}</h2>
          <form className="inline-form" onSubmit={handleCreateBudget}>
            <input
              type="text"
              placeholder={t("budgets.budgetName")}
              value={budgetName}
              onChange={(event) => setBudgetName(event.target.value)}
              required
            />
            <input
              type="month"
              value={budgetMonth}
              onChange={(event) => setBudgetMonth(event.target.value)}
              required
            />
            <button type="submit" className="inline-button">
              {t("common.create")}
            </button>
          </form>
        </section>
      ) : null}

      <section className="card card-wide">
        <h2>{t("budgets.budgets")}</h2>
        {budgets.length === 0 ? (
          <p className="muted">{t("budgets.noBudgets")}</p>
        ) : (
          <>
            <label>
              {t("budgets.selectedBudget")}
              <select
                value={selectedBudgetId}
                onChange={(event) => setSelectedBudgetId(event.target.value)}
              >
                {budgets.map((budget) => (
                  <option key={budget.id} value={budget.id}>
                    {budget.name} ({budget.periodStart})
                  </option>
                ))}
              </select>
            </label>
            {summary ? (
              <dl className="metric-list">
                <div>
                  <dt>{t("budgets.totalBudget")}</dt>
                  <dd>{formatCents(summary.totalBudgetCents, "EUR", locale)}</dd>
                </div>
                <div>
                  <dt>{t("budgets.totalExpenses")}</dt>
                  <dd>{formatCents(summary.totalExpenseCents, "EUR", locale)}</dd>
                </div>
                <div>
                  <dt>{t("budgets.remaining")}</dt>
                  <dd>{formatCents(summary.totalBudgetCents - summary.totalExpenseCents, "EUR", locale)}</dd>
                </div>
              </dl>
            ) : null}
          </>
        )}
      </section>

      {canWrite && selectedBudgetId && categories.length > 0 ? (
        <section className="card card-wide">
          <h2>{t("budgets.budgetItem")}</h2>
          <form className="inline-form" onSubmit={handleUpsertItem}>
            <select
              value={itemCategoryId}
              onChange={(event) => setItemCategoryId(event.target.value)}
              required
            >
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
            <input
              type="number"
              step="0.01"
              min="0"
              placeholder={t("common.amount")}
              value={itemAmount}
              onChange={(event) => setItemAmount(event.target.value)}
              required
            />
            <button type="submit" className="inline-button">
              {t("budgets.saveItem")}
            </button>
          </form>
        </section>
      ) : null}
    </div>
  );
}
