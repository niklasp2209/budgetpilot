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
import { useOrganization } from "@/features/organization/context/OrganizationProvider";
import type { Budget, BudgetSummary, Category } from "@/shared/types/api";

export function BudgetsView() {
  const { selectedOrganization } = useOrganization();
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
      setError(caught instanceof ApiError ? caught.message : "Failed to load budgets.");
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
      setError(caught instanceof ApiError ? caught.message : "Failed to load budget summary.");
    }
  }

  useEffect(() => {
    void loadBudgets();
  }, [selectedOrganization?.id]);

  useEffect(() => {
    void loadSummary(selectedBudgetId);
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
      setError(caught instanceof ApiError ? caught.message : "Failed to create budget.");
    }
  }

  async function handleUpsertItem(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedOrganization || !selectedBudgetId) {
      return;
    }
    const amountCents = Math.round(Number.parseFloat(itemAmount) * 100);
    if (!Number.isFinite(amountCents) || amountCents < 0) {
      setError("Budget amount must be zero or greater.");
      return;
    }
    setError(null);
    try {
      await upsertBudgetItem(selectedOrganization.id, selectedBudgetId, itemCategoryId, amountCents);
      setItemAmount("");
      await loadSummary(selectedBudgetId);
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Failed to save budget item.");
    }
  }

  if (!selectedOrganization) {
    return null;
  }

  if (isLoading) {
    return <p className="muted">Loading budgets...</p>;
  }

  return (
    <div className="stack">
      {error ? <p className="error">{error}</p> : null}

      {canWrite ? (
        <section className="card card-wide">
          <h2>Create budget</h2>
          <form className="inline-form" onSubmit={handleCreateBudget}>
            <input
              type="text"
              placeholder="Budget name"
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
              Create
            </button>
          </form>
        </section>
      ) : null}

      <section className="card card-wide">
        <h2>Budgets</h2>
        {budgets.length === 0 ? (
          <p className="muted">No budgets yet.</p>
        ) : (
          <>
            <label>
              Selected budget
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
                  <dt>Total budget</dt>
                  <dd>{formatCents(summary.totalBudgetCents)}</dd>
                </div>
                <div>
                  <dt>Total expenses</dt>
                  <dd>{formatCents(summary.totalExpenseCents)}</dd>
                </div>
                <div>
                  <dt>Remaining</dt>
                  <dd>{formatCents(summary.totalBudgetCents - summary.totalExpenseCents)}</dd>
                </div>
              </dl>
            ) : null}
          </>
        )}
      </section>

      {canWrite && selectedBudgetId && categories.length > 0 ? (
        <section className="card card-wide">
          <h2>Budget item</h2>
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
              placeholder="Amount"
              value={itemAmount}
              onChange={(event) => setItemAmount(event.target.value)}
              required
            />
            <button type="submit" className="inline-button">
              Save item
            </button>
          </form>
        </section>
      ) : null}
    </div>
  );
}
