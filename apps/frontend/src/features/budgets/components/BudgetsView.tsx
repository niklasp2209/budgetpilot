"use client";

import { FormEvent, useEffect, useState } from "react";
import { ApiError } from "@/shared/api/client";
import {
  createBudget,
  deleteBudgetItem,
  fetchBudgetItems,
  fetchBudgetSummary,
  fetchBudgets,
  upsertBudgetItem
} from "@/shared/api/budgets";
import { fetchCategories } from "@/shared/api/accounting";
import { EmptyState } from "@/shared/components/EmptyState";
import { formatCents } from "@/shared/lib/format";
import { hasPermission } from "@/shared/lib/permissions";
import { runInEffectAsync } from "@/shared/lib/runInEffectAsync";
import { useTranslation } from "@/features/i18n/context/I18nProvider";
import { useOrganization } from "@/features/organization/context/OrganizationProvider";
import type { Budget, BudgetItem, BudgetSummary, Category } from "@/shared/types/api";

export function BudgetsView() {
  const { selectedOrganization } = useOrganization();
  const { t, locale } = useTranslation();
  const [budgets, setBudgets] = useState<Budget[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [items, setItems] = useState<BudgetItem[]>([]);
  const [selectedBudgetId, setSelectedBudgetId] = useState<string>("");
  const [summary, setSummary] = useState<BudgetSummary | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingItems, setIsLoadingItems] = useState(false);

  const canWrite = hasPermission(selectedOrganization, "BUDGET_WRITE");

  const [budgetName, setBudgetName] = useState("");
  const [budgetMonth, setBudgetMonth] = useState(() => new Date().toISOString().slice(0, 7));
  const [itemCategoryId, setItemCategoryId] = useState("");
  const [itemAmount, setItemAmount] = useState("");

  const [editingItemId, setEditingItemId] = useState<string | null>(null);
  const [editAmount, setEditAmount] = useState("");

  async function loadBudgetData() {
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

      if (loadedCategories.length > 0) {
        setItemCategoryId((current) => current || loadedCategories[0].id);
      }
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : t("budgets.loadFailed"));
    } finally {
      setIsLoading(false);
    }
  }

  async function loadBudgetDetails(budgetId: string) {
    if (!selectedOrganization || !budgetId) {
      setSummary(null);
      setItems([]);
      return;
    }
    setIsLoadingItems(true);
    setError(null);
    try {
      const [loadedSummary, loadedItems] = await Promise.all([
        fetchBudgetSummary(selectedOrganization.id, budgetId),
        fetchBudgetItems(selectedOrganization.id, budgetId)
      ]);
      setSummary(loadedSummary);
      setItems(loadedItems);
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : t("budgets.loadItemsFailed"));
    } finally {
      setIsLoadingItems(false);
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
    if (!organizationId || isLoading) {
      return;
    }

    if (!selectedBudgetId) {
      return runInEffectAsync(async (isCancelled) => {
        if (!isCancelled()) {
          setSummary(null);
          setItems([]);
        }
      });
    }

    return runInEffectAsync(async (isCancelled) => {
      setIsLoadingItems(true);
      setError(null);
      try {
        const [loadedSummary, loadedItems] = await Promise.all([
          fetchBudgetSummary(organizationId, selectedBudgetId),
          fetchBudgetItems(organizationId, selectedBudgetId)
        ]);
        if (!isCancelled()) {
          setSummary(loadedSummary);
          setItems(loadedItems);
        }
      } catch (caught) {
        if (!isCancelled()) {
          setError(caught instanceof ApiError ? caught.message : t("budgets.loadItemsFailed"));
        }
      } finally {
        if (!isCancelled()) {
          setIsLoadingItems(false);
        }
      }
    });
  }, [selectedOrganization?.id, selectedBudgetId, isLoading]);

  function startEditing(item: BudgetItem) {
    setEditingItemId(item.id);
    setEditAmount((item.amountCents / 100).toFixed(2));
  }

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
      await loadBudgetData();
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
      await loadBudgetDetails(selectedBudgetId);
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : t("budgets.saveItemFailed"));
    }
  }

  async function handleUpdateItem(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedOrganization || !selectedBudgetId || !editingItemId) {
      return;
    }
    const amountCents = Math.round(Number.parseFloat(editAmount) * 100);
    if (!Number.isFinite(amountCents) || amountCents < 0) {
      setError(t("budgets.amountNonNegative"));
      return;
    }
    setError(null);
    try {
      const item = items.find((entry) => entry.id === editingItemId);
      if (!item) {
        return;
      }
      await upsertBudgetItem(selectedOrganization.id, selectedBudgetId, item.categoryId, amountCents);
      setEditingItemId(null);
      await loadBudgetDetails(selectedBudgetId);
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : t("budgets.updateItemFailed"));
    }
  }

  async function handleDeleteItem(itemId: string) {
    if (!selectedOrganization || !selectedBudgetId) {
      return;
    }
    setError(null);
    try {
      await deleteBudgetItem(selectedOrganization.id, selectedBudgetId, itemId);
      if (editingItemId === itemId) {
        setEditingItemId(null);
      }
      await loadBudgetDetails(selectedBudgetId);
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : t("budgets.deleteItemFailed"));
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

      {selectedBudgetId ? (
        <section className="card card-wide">
          <h2>{t("budgets.budgetItems")}</h2>
          {isLoadingItems ? (
            <p className="muted">{t("budgets.loadingItems")}</p>
          ) : items.length === 0 ? (
            <EmptyState
              title={t("budgets.noBudgetItems")}
              description={t("budgets.noBudgetItemsDescription")}
            />
          ) : (
            <table className="data-table">
              <thead>
                <tr>
                  <th>{t("common.category")}</th>
                  <th>{t("common.amount")}</th>
                  {canWrite ? <th /> : null}
                </tr>
              </thead>
              <tbody>
                {items.map((item) =>
                  editingItemId === item.id ? (
                    <tr key={item.id}>
                      <td colSpan={canWrite ? 3 : 2}>
                        <form className="stack-form" onSubmit={handleUpdateItem}>
                          <div className="inline-form">
                            <span>{item.categoryName}</span>
                            <input
                              type="number"
                              step="0.01"
                              min="0"
                              value={editAmount}
                              onChange={(event) => setEditAmount(event.target.value)}
                              required
                            />
                          </div>
                          <div className="row-actions">
                            <button type="submit" className="inline-button">
                              {t("common.save")}
                            </button>
                            <button
                              type="button"
                              className="secondary-button"
                              onClick={() => setEditingItemId(null)}
                            >
                              {t("common.cancel")}
                            </button>
                          </div>
                        </form>
                      </td>
                    </tr>
                  ) : (
                    <tr key={item.id}>
                      <td>{item.categoryName}</td>
                      <td>{formatCents(item.amountCents, "EUR", locale)}</td>
                      {canWrite ? (
                        <td>
                          <div className="row-actions">
                            <button
                              type="button"
                              className="secondary-button"
                              onClick={() => startEditing(item)}
                            >
                              {t("common.edit")}
                            </button>
                            <button
                              type="button"
                              className="danger-button"
                              onClick={() => void handleDeleteItem(item.id)}
                            >
                              {t("common.delete")}
                            </button>
                          </div>
                        </td>
                      ) : null}
                    </tr>
                  )
                )}
              </tbody>
            </table>
          )}

          {canWrite && categories.length > 0 ? (
            <form className="stack-form" onSubmit={handleUpsertItem}>
              <h3>{t("budgets.addBudgetItem")}</h3>
              <div className="inline-form">
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
              </div>
            </form>
          ) : null}
        </section>
      ) : null}
    </div>
  );
}
