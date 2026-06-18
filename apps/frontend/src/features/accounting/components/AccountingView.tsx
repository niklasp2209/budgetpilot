"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";
import { ApiError } from "@/shared/api/client";
import {
  createAccount,
  createCategory,
  createTransaction,
  deleteAccount,
  deleteCategory,
  deleteTransaction,
  fetchAccounts,
  fetchCategories,
  fetchTransactions,
  updateTransaction
} from "@/shared/api/accounting";
import { DateRangeFilter } from "@/shared/components/DateRangeFilter";
import { EmptyState } from "@/shared/components/EmptyState";
import {
  defaultCustomFromDate,
  defaultCustomToDate,
  resolveDateRangeIso,
  toLocalDateTimeInput,
  type DateRangePreset
} from "@/shared/lib/dateRange";
import { formatCents } from "@/shared/lib/format";
import { hasPermission } from "@/shared/lib/permissions";
import { useOrganization } from "@/features/organization/context/OrganizationProvider";
import type { Account, Category, CategoryType, Transaction } from "@/shared/types/api";

function toBookedAtIso(localDateTime: string): string | undefined {
  if (!localDateTime.trim()) {
    return undefined;
  }
  return new Date(localDateTime).toISOString();
}

export function AccountingView() {
  const { selectedOrganization } = useOrganization();
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingTransactions, setIsLoadingTransactions] = useState(false);

  const canWrite = hasPermission(selectedOrganization, "ACCOUNTING_WRITE");

  const [accountName, setAccountName] = useState("");
  const [accountCurrency, setAccountCurrency] = useState("EUR");
  const [categoryName, setCategoryName] = useState("");
  const [categoryType, setCategoryType] = useState<CategoryType>("EXPENSE");

  const [transactionAccountId, setTransactionAccountId] = useState("");
  const [transactionCategoryId, setTransactionCategoryId] = useState("");
  const [transactionAmount, setTransactionAmount] = useState("");
  const [transactionBookedAt, setTransactionBookedAt] = useState("");
  const [transactionDescription, setTransactionDescription] = useState("");

  const [filterPreset, setFilterPreset] = useState<DateRangePreset>("all");
  const [filterCustomFrom, setFilterCustomFrom] = useState(defaultCustomFromDate);
  const [filterCustomTo, setFilterCustomTo] = useState(defaultCustomToDate);
  const [filterAccountId, setFilterAccountId] = useState("");
  const [filterCategoryId, setFilterCategoryId] = useState("");

  const [editingTransactionId, setEditingTransactionId] = useState<string | null>(null);
  const [editAccountId, setEditAccountId] = useState("");
  const [editCategoryId, setEditCategoryId] = useState("");
  const [editAmount, setEditAmount] = useState("");
  const [editBookedAt, setEditBookedAt] = useState("");
  const [editDescription, setEditDescription] = useState("");

  const filterRange = useMemo(
    () => resolveDateRangeIso(filterPreset, filterCustomFrom, filterCustomTo),
    [filterPreset, filterCustomFrom, filterCustomTo]
  );

  async function loadBaseData() {
    if (!selectedOrganization) {
      return;
    }
    setIsLoading(true);
    setError(null);
    try {
      const [loadedAccounts, loadedCategories] = await Promise.all([
        fetchAccounts(selectedOrganization.id),
        fetchCategories(selectedOrganization.id)
      ]);
      setAccounts(loadedAccounts);
      setCategories(loadedCategories);
      if (loadedAccounts.length > 0 && !transactionAccountId) {
        setTransactionAccountId(loadedAccounts[0].id);
      }
      if (loadedCategories.length > 0 && !transactionCategoryId) {
        setTransactionCategoryId(loadedCategories[0].id);
      }
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Failed to load accounting data.");
    } finally {
      setIsLoading(false);
    }
  }

  async function loadTransactions() {
    if (!selectedOrganization) {
      return;
    }
    setIsLoadingTransactions(true);
    setError(null);
    try {
      const loadedTransactions = await fetchTransactions(selectedOrganization.id, {
        from: filterRange.from,
        to: filterRange.to,
        accountId: filterAccountId || undefined,
        categoryId: filterCategoryId || undefined
      });
      setTransactions(loadedTransactions);
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Failed to load transactions.");
    } finally {
      setIsLoadingTransactions(false);
    }
  }

  useEffect(() => {
    void loadBaseData();
  }, [selectedOrganization?.id]);

  useEffect(() => {
    if (!selectedOrganization || isLoading) {
      return;
    }
    void loadTransactions();
  }, [
    selectedOrganization?.id,
    filterRange.from,
    filterRange.to,
    filterAccountId,
    filterCategoryId,
    isLoading
  ]);

  function startEditing(transaction: Transaction) {
    setEditingTransactionId(transaction.id);
    setEditAccountId(transaction.accountId);
    setEditCategoryId(transaction.categoryId);
    setEditAmount((transaction.amountCents / 100).toFixed(2));
    setEditBookedAt(toLocalDateTimeInput(transaction.bookedAt));
    setEditDescription(transaction.description ?? "");
  }

  async function handleCreateAccount(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedOrganization) {
      return;
    }
    setError(null);
    try {
      await createAccount(selectedOrganization.id, accountName.trim(), accountCurrency.trim().toUpperCase());
      setAccountName("");
      await loadBaseData();
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Failed to create account.");
    }
  }

  async function handleDeleteAccount(accountId: string) {
    if (!selectedOrganization) {
      return;
    }
    setError(null);
    try {
      await deleteAccount(selectedOrganization.id, accountId);
      await loadBaseData();
      await loadTransactions();
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Failed to delete account.");
    }
  }

  async function handleCreateCategory(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedOrganization) {
      return;
    }
    setError(null);
    try {
      await createCategory(selectedOrganization.id, categoryName.trim(), categoryType);
      setCategoryName("");
      await loadBaseData();
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Failed to create category.");
    }
  }

  async function handleDeleteCategory(categoryId: string) {
    if (!selectedOrganization) {
      return;
    }
    setError(null);
    try {
      await deleteCategory(selectedOrganization.id, categoryId);
      await loadBaseData();
      await loadTransactions();
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Failed to delete category.");
    }
  }

  async function handleCreateTransaction(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedOrganization) {
      return;
    }
    const account = accounts.find((entry) => entry.id === transactionAccountId);
    if (!account) {
      return;
    }
    const amountCents = Math.round(Number.parseFloat(transactionAmount) * 100);
    if (!Number.isFinite(amountCents) || amountCents <= 0) {
      setError("Amount must be greater than zero.");
      return;
    }
    const bookedAt = toBookedAtIso(transactionBookedAt);
    if (transactionBookedAt.trim() && !bookedAt) {
      setError("Invalid date and time.");
      return;
    }
    setError(null);
    try {
      await createTransaction(selectedOrganization.id, {
        accountId: transactionAccountId,
        categoryId: transactionCategoryId,
        amountCents,
        currency: account.currency,
        bookedAt,
        description: transactionDescription.trim() || undefined
      });
      setTransactionAmount("");
      setTransactionBookedAt("");
      setTransactionDescription("");
      await loadTransactions();
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Failed to create transaction.");
    }
  }

  async function handleUpdateTransaction(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedOrganization || !editingTransactionId) {
      return;
    }
    const amountCents = Math.round(Number.parseFloat(editAmount) * 100);
    if (!Number.isFinite(amountCents) || amountCents <= 0) {
      setError("Amount must be greater than zero.");
      return;
    }
    const bookedAt = toBookedAtIso(editBookedAt);
    if (!bookedAt) {
      setError("Invalid date and time.");
      return;
    }
    setError(null);
    try {
      await updateTransaction(selectedOrganization.id, editingTransactionId, {
        accountId: editAccountId,
        categoryId: editCategoryId,
        amountCents,
        bookedAt,
        description: editDescription.trim() || undefined
      });
      setEditingTransactionId(null);
      await loadTransactions();
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Failed to update transaction.");
    }
  }

  async function handleDeleteTransaction(transactionId: string) {
    if (!selectedOrganization) {
      return;
    }
    setError(null);
    try {
      await deleteTransaction(selectedOrganization.id, transactionId);
      if (editingTransactionId === transactionId) {
        setEditingTransactionId(null);
      }
      await loadTransactions();
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Failed to delete transaction.");
    }
  }

  if (!selectedOrganization) {
    return null;
  }

  if (isLoading) {
    return <p className="muted">Loading accounting...</p>;
  }

  const canAddTransactions = accounts.length > 0 && categories.length > 0;

  return (
    <div className="stack">
      {error ? <p className="error">{error}</p> : null}

      <section className="card card-wide">
        <h2>Accounts</h2>
        {accounts.length === 0 ? (
          <EmptyState
            title="Create your first account"
            description="Accounts represent bank or cash wallets. Add one before recording transactions."
          />
        ) : (
          <ul className="data-list">
            {accounts.map((account) => (
              <li key={account.id}>
                <span>{account.name}</span>
                <div className="row-actions">
                  <span>{account.currency}</span>
                  {canWrite ? (
                    <button
                      type="button"
                      className="danger-button"
                      onClick={() => void handleDeleteAccount(account.id)}
                    >
                      Delete
                    </button>
                  ) : null}
                </div>
              </li>
            ))}
          </ul>
        )}
        {canWrite ? (
          <form className="inline-form" onSubmit={handleCreateAccount}>
            <input
              type="text"
              placeholder="Account name"
              value={accountName}
              onChange={(event) => setAccountName(event.target.value)}
              required
            />
            <input
              type="text"
              placeholder="EUR"
              value={accountCurrency}
              onChange={(event) => setAccountCurrency(event.target.value)}
              required
              maxLength={3}
            />
            <button type="submit" className="inline-button">
              Add account
            </button>
          </form>
        ) : null}
      </section>

      <section className="card card-wide">
        <h2>Categories</h2>
        {categories.length === 0 ? (
          <EmptyState
            title="Create categories next"
            description="Categories classify income and expenses. Add at least one before creating transactions."
          />
        ) : (
          <ul className="data-list">
            {categories.map((category) => (
              <li key={category.id}>
                <span>{category.name}</span>
                <div className="row-actions">
                  <span>{category.type}</span>
                  {canWrite ? (
                    <button
                      type="button"
                      className="danger-button"
                      onClick={() => void handleDeleteCategory(category.id)}
                    >
                      Delete
                    </button>
                  ) : null}
                </div>
              </li>
            ))}
          </ul>
        )}
        {canWrite ? (
          <form className="inline-form" onSubmit={handleCreateCategory}>
            <input
              type="text"
              placeholder="Category name"
              value={categoryName}
              onChange={(event) => setCategoryName(event.target.value)}
              required
            />
            <select
              value={categoryType}
              onChange={(event) => setCategoryType(event.target.value as CategoryType)}
            >
              <option value="EXPENSE">EXPENSE</option>
              <option value="INCOME">INCOME</option>
              <option value="TRANSFER">TRANSFER</option>
            </select>
            <button type="submit" className="inline-button">
              Add category
            </button>
          </form>
        ) : null}
      </section>

      <section className="card card-wide">
        <h2>Transactions</h2>

        <div className="filter-bar">
          <DateRangeFilter
            preset={filterPreset}
            customFrom={filterCustomFrom}
            customTo={filterCustomTo}
            onPresetChange={setFilterPreset}
            onCustomFromChange={setFilterCustomFrom}
            onCustomToChange={setFilterCustomTo}
          />
          <select value={filterAccountId} onChange={(event) => setFilterAccountId(event.target.value)}>
            <option value="">All accounts</option>
            {accounts.map((account) => (
              <option key={account.id} value={account.id}>
                {account.name}
              </option>
            ))}
          </select>
          <select value={filterCategoryId} onChange={(event) => setFilterCategoryId(event.target.value)}>
            <option value="">All categories</option>
            {categories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </div>

        {isLoadingTransactions ? (
          <p className="muted">Loading transactions...</p>
        ) : transactions.length === 0 ? (
          <EmptyState
            title="No transactions found"
            description={
              canAddTransactions
                ? "No transactions match your filters, or none have been recorded yet."
                : "Create an account and at least one category before adding transactions."
            }
          />
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Description</th>
                <th>Amount</th>
                <th>Date</th>
                {canWrite ? <th /> : null}
              </tr>
            </thead>
            <tbody>
              {transactions.map((transaction) =>
                editingTransactionId === transaction.id ? (
                  <tr key={transaction.id}>
                    <td colSpan={canWrite ? 4 : 3}>
                      <form className="stack-form" onSubmit={handleUpdateTransaction}>
                        <div className="inline-form">
                          <select
                            value={editAccountId}
                            onChange={(event) => setEditAccountId(event.target.value)}
                            required
                          >
                            {accounts.map((account) => (
                              <option key={account.id} value={account.id}>
                                {account.name}
                              </option>
                            ))}
                          </select>
                          <select
                            value={editCategoryId}
                            onChange={(event) => setEditCategoryId(event.target.value)}
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
                            min="0.01"
                            value={editAmount}
                            onChange={(event) => setEditAmount(event.target.value)}
                            required
                          />
                          <input
                            type="datetime-local"
                            value={editBookedAt}
                            onChange={(event) => setEditBookedAt(event.target.value)}
                            required
                          />
                        </div>
                        <input
                          type="text"
                          placeholder="Description"
                          value={editDescription}
                          onChange={(event) => setEditDescription(event.target.value)}
                        />
                        <div className="row-actions">
                          <button type="submit" className="inline-button">
                            Save
                          </button>
                          <button
                            type="button"
                            className="secondary-button"
                            onClick={() => setEditingTransactionId(null)}
                          >
                            Cancel
                          </button>
                        </div>
                      </form>
                    </td>
                  </tr>
                ) : (
                  <tr key={transaction.id}>
                    <td>{transaction.description ?? "-"}</td>
                    <td>{formatCents(transaction.amountCents, transaction.currency)}</td>
                    <td>{new Date(transaction.bookedAt).toLocaleString("de-DE")}</td>
                    {canWrite ? (
                      <td>
                        <div className="row-actions">
                          <button
                            type="button"
                            className="secondary-button"
                            onClick={() => startEditing(transaction)}
                          >
                            Edit
                          </button>
                          <button
                            type="button"
                            className="danger-button"
                            onClick={() => void handleDeleteTransaction(transaction.id)}
                          >
                            Delete
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

        {canWrite ? (
          canAddTransactions ? (
            <form className="stack-form" onSubmit={handleCreateTransaction}>
              <div className="inline-form">
                <select
                  value={transactionAccountId}
                  onChange={(event) => setTransactionAccountId(event.target.value)}
                  required
                >
                  {accounts.map((account) => (
                    <option key={account.id} value={account.id}>
                      {account.name}
                    </option>
                  ))}
                </select>
                <select
                  value={transactionCategoryId}
                  onChange={(event) => setTransactionCategoryId(event.target.value)}
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
                  min="0.01"
                  placeholder="Amount"
                  value={transactionAmount}
                  onChange={(event) => setTransactionAmount(event.target.value)}
                  required
                />
              </div>
              <label>
                Date and time (optional)
                <input
                  type="datetime-local"
                  value={transactionBookedAt}
                  onChange={(event) => setTransactionBookedAt(event.target.value)}
                />
              </label>
              <input
                type="text"
                placeholder="Description"
                value={transactionDescription}
                onChange={(event) => setTransactionDescription(event.target.value)}
              />
              <button type="submit" className="inline-button">
                Add transaction
              </button>
            </form>
          ) : (
            <EmptyState
              title="Transactions not available yet"
              description="Add at least one account and one category before recording transactions."
            />
          )
        ) : (
          <p className="muted">Read-only access for your role.</p>
        )}
      </section>
    </div>
  );
}
