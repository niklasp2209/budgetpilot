"use client";

import { FormEvent, useEffect, useState } from "react";
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
  fetchTransactions
} from "@/shared/api/accounting";
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

  async function loadData() {
    if (!selectedOrganization) {
      return;
    }
    setIsLoading(true);
    setError(null);
    try {
      const [loadedAccounts, loadedCategories, loadedTransactions] = await Promise.all([
        fetchAccounts(selectedOrganization.id),
        fetchCategories(selectedOrganization.id),
        fetchTransactions(selectedOrganization.id)
      ]);
      setAccounts(loadedAccounts);
      setCategories(loadedCategories);
      setTransactions(loadedTransactions);
      if (loadedAccounts.length > 0 && !transactionAccountId) {
        setTransactionAccountId(loadedAccounts[0].id);
      }
      if (loadedCategories.length > 0 && !transactionCategoryId) {
        setTransactionCategoryId(loadedCategories[0].id);
      }
    } catch (caught) {
      if (caught instanceof ApiError) {
        setError(caught.message);
      } else {
        setError("Failed to load accounting data.");
      }
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void loadData();
  }, [selectedOrganization?.id]);

  async function handleCreateAccount(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedOrganization) {
      return;
    }
    setError(null);
    try {
      await createAccount(selectedOrganization.id, accountName.trim(), accountCurrency.trim().toUpperCase());
      setAccountName("");
      await loadData();
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
      await loadData();
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
      await loadData();
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
      await loadData();
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
      await loadData();
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : "Failed to create transaction.");
    }
  }

  async function handleDeleteTransaction(transactionId: string) {
    if (!selectedOrganization) {
      return;
    }
    setError(null);
    try {
      await deleteTransaction(selectedOrganization.id, transactionId);
      await loadData();
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

  return (
    <div className="stack">
      {error ? <p className="error">{error}</p> : null}

      <section className="card card-wide">
        <h2>Accounts</h2>
        {accounts.length === 0 ? (
          <p className="muted">No accounts yet.</p>
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
          <p className="muted">No categories yet.</p>
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
        {transactions.length === 0 ? (
          <p className="muted">No transactions yet.</p>
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
              {transactions.map((transaction) => (
                <tr key={transaction.id}>
                  <td>{transaction.description ?? "-"}</td>
                  <td>{formatCents(transaction.amountCents, transaction.currency)}</td>
                  <td>{new Date(transaction.bookedAt).toLocaleString("de-DE")}</td>
                  {canWrite ? (
                    <td>
                      <button
                        type="button"
                        className="danger-button"
                        onClick={() => void handleDeleteTransaction(transaction.id)}
                      >
                        Delete
                      </button>
                    </td>
                  ) : null}
                </tr>
              ))}
            </tbody>
          </table>
        )}
        {canWrite ? (
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
          <p className="muted">Read-only access for your role.</p>
        )}
      </section>
    </div>
  );
}
