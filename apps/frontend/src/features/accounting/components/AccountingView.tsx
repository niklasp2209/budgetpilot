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
  formatDateTime,
  resolveDateRangeIso,
  toLocalDateTimeInput,
  type DateRangePreset
} from "@/shared/lib/dateRange";
import { getCurrencySymbol } from "@/shared/lib/currencies";
import { formatCents } from "@/shared/lib/format";
import { hasPermission } from "@/shared/lib/permissions";
import { runInEffectAsync } from "@/shared/lib/runInEffectAsync";
import { useTranslation } from "@/features/i18n/context/I18nProvider";
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
  const { t, locale } = useTranslation();
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingTransactions, setIsLoadingTransactions] = useState(false);

  const canWrite = hasPermission(selectedOrganization, "ACCOUNTING_WRITE");

  const [accountName, setAccountName] = useState("");
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
      setError(caught instanceof ApiError ? caught.message : t("accounting.loadFailed"));
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
      setError(caught instanceof ApiError ? caught.message : t("accounting.loadTransactionsFailed"));
    } finally {
      setIsLoadingTransactions(false);
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
        const [loadedAccounts, loadedCategories] = await Promise.all([
          fetchAccounts(organizationId),
          fetchCategories(organizationId)
        ]);
        if (isCancelled()) {
          return;
        }
        setAccounts(loadedAccounts);
        setCategories(loadedCategories);
        if (loadedAccounts.length > 0) {
          setTransactionAccountId((current) => current || loadedAccounts[0].id);
        }
        if (loadedCategories.length > 0) {
          setTransactionCategoryId((current) => current || loadedCategories[0].id);
        }
      } catch (caught) {
        if (!isCancelled()) {
          setError(caught instanceof ApiError ? caught.message : t("accounting.loadFailed"));
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

    return runInEffectAsync(async (isCancelled) => {
      setIsLoadingTransactions(true);
      setError(null);
      try {
        const loadedTransactions = await fetchTransactions(organizationId, {
          from: filterRange.from,
          to: filterRange.to,
          accountId: filterAccountId || undefined,
          categoryId: filterCategoryId || undefined
        });
        if (!isCancelled()) {
          setTransactions(loadedTransactions);
        }
      } catch (caught) {
        if (!isCancelled()) {
          setError(caught instanceof ApiError ? caught.message : t("accounting.loadTransactionsFailed"));
        }
      } finally {
        if (!isCancelled()) {
          setIsLoadingTransactions(false);
        }
      }
    });
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
      await createAccount(selectedOrganization.id, accountName.trim());
      setAccountName("");
      await loadBaseData();
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : t("accounting.createAccountFailed"));
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
      setError(caught instanceof ApiError ? caught.message : t("accounting.deleteAccountFailed"));
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
      setError(caught instanceof ApiError ? caught.message : t("accounting.createCategoryFailed"));
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
      setError(caught instanceof ApiError ? caught.message : t("accounting.deleteCategoryFailed"));
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
      setError(t("accounting.amountPositive"));
      return;
    }
    const bookedAt = toBookedAtIso(transactionBookedAt);
    if (transactionBookedAt.trim() && !bookedAt) {
      setError(t("accounting.invalidDateTime"));
      return;
    }
    setError(null);
    try {
      await createTransaction(selectedOrganization.id, {
        accountId: transactionAccountId,
        categoryId: transactionCategoryId,
        amountCents,
        currency: selectedOrganization.currency,
        bookedAt,
        description: transactionDescription.trim() || undefined
      });
      setTransactionAmount("");
      setTransactionBookedAt("");
      setTransactionDescription("");
      await loadTransactions();
    } catch (caught) {
      setError(caught instanceof ApiError ? caught.message : t("accounting.createTransactionFailed"));
    }
  }

  async function handleUpdateTransaction(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedOrganization || !editingTransactionId) {
      return;
    }
    const amountCents = Math.round(Number.parseFloat(editAmount) * 100);
    if (!Number.isFinite(amountCents) || amountCents <= 0) {
      setError(t("accounting.amountPositive"));
      return;
    }
    const bookedAt = toBookedAtIso(editBookedAt);
    if (!bookedAt) {
      setError(t("accounting.invalidDateTime"));
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
      setError(caught instanceof ApiError ? caught.message : t("accounting.updateTransactionFailed"));
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
      setError(caught instanceof ApiError ? caught.message : t("accounting.deleteTransactionFailed"));
    }
  }

  if (!selectedOrganization) {
    return null;
  }

  if (isLoading) {
    return <p className="muted">{t("accounting.loading")}</p>;
  }

  const canAddTransactions = accounts.length > 0 && categories.length > 0;

  return (
    <div className="stack">
      {error ? <p className="error">{error}</p> : null}

      <section className="card card-wide">
        <h2>{t("accounting.accounts")}</h2>
        {accounts.length === 0 ? (
          <EmptyState
            title={t("accounting.createAccountTitle")}
            description={t("accounting.createAccountDescription")}
          />
        ) : (
          <ul className="data-list">
            {accounts.map((account) => (
              <li key={account.id}>
                <span>{account.name}</span>
                <div className="row-actions">
                  <span>{getCurrencySymbol(account.currency)}</span>
                  {canWrite ? (
                    <button
                      type="button"
                      className="danger-button"
                      onClick={() => void handleDeleteAccount(account.id)}
                    >
                      {t("common.delete")}
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
              placeholder={t("accounting.accountName")}
              value={accountName}
              onChange={(event) => setAccountName(event.target.value)}
              required
            />
            <button type="submit" className="inline-button">
              {t("accounting.addAccount")}
            </button>
          </form>
        ) : null}
      </section>

      <section className="card card-wide">
        <h2>{t("accounting.categories")}</h2>
        {categories.length === 0 ? (
          <EmptyState
            title={t("accounting.createCategoryTitle")}
            description={t("accounting.createCategoryDescription")}
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
                      {t("common.delete")}
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
              placeholder={t("accounting.categoryName")}
              value={categoryName}
              onChange={(event) => setCategoryName(event.target.value)}
              required
            />
            <select
              value={categoryType}
              onChange={(event) => setCategoryType(event.target.value as CategoryType)}
            >
              <option value="EXPENSE">{t("accounting.categoryExpense")}</option>
              <option value="INCOME">{t("accounting.categoryIncome")}</option>
              <option value="TRANSFER">{t("accounting.categoryTransfer")}</option>
            </select>
            <button type="submit" className="inline-button">
              {t("accounting.addCategory")}
            </button>
          </form>
        ) : null}
      </section>

      <section className="card card-wide">
        <h2>{t("accounting.transactions")}</h2>

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
            <option value="">{t("common.allAccounts")}</option>
            {accounts.map((account) => (
              <option key={account.id} value={account.id}>
                {account.name}
              </option>
            ))}
          </select>
          <select value={filterCategoryId} onChange={(event) => setFilterCategoryId(event.target.value)}>
            <option value="">{t("common.allCategories")}</option>
            {categories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </div>

        {isLoadingTransactions ? (
          <p className="muted">{t("accounting.loadingTransactions")}</p>
        ) : transactions.length === 0 ? (
          <EmptyState
            title={t("accounting.noTransactionsTitle")}
            description={
              canAddTransactions
                ? t("accounting.noTransactionsFiltered")
                : t("accounting.noTransactionsSetup")
            }
          />
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>{t("common.description")}</th>
                <th>{t("common.amount")}</th>
                <th>{t("common.date")}</th>
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
                          placeholder={t("common.description")}
                          value={editDescription}
                          onChange={(event) => setEditDescription(event.target.value)}
                        />
                        <div className="row-actions">
                          <button type="submit" className="inline-button">
                            {t("common.save")}
                          </button>
                          <button
                            type="button"
                            className="secondary-button"
                            onClick={() => setEditingTransactionId(null)}
                          >
                            {t("common.cancel")}
                          </button>
                        </div>
                      </form>
                    </td>
                  </tr>
                ) : (
                  <tr key={transaction.id}>
                    <td>{transaction.description ?? "-"}</td>
                    <td>{formatCents(transaction.amountCents, transaction.currency, locale)}</td>
                    <td>{formatDateTime(transaction.bookedAt, locale)}</td>
                    {canWrite ? (
                      <td>
                        <div className="row-actions">
                          <button
                            type="button"
                            className="secondary-button"
                            onClick={() => startEditing(transaction)}
                          >
                            {t("common.edit")}
                          </button>
                          <button
                            type="button"
                            className="danger-button"
                            onClick={() => void handleDeleteTransaction(transaction.id)}
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
                  placeholder={t("common.amount")}
                  value={transactionAmount}
                  onChange={(event) => setTransactionAmount(event.target.value)}
                  required
                />
              </div>
              <label>
                {t("accounting.dateTimeOptional")}
                <input
                  type="datetime-local"
                  value={transactionBookedAt}
                  onChange={(event) => setTransactionBookedAt(event.target.value)}
                />
              </label>
              <input
                type="text"
                placeholder={t("common.description")}
                value={transactionDescription}
                onChange={(event) => setTransactionDescription(event.target.value)}
              />
              <button type="submit" className="inline-button">
                {t("accounting.addTransaction")}
              </button>
            </form>
          ) : (
            <EmptyState
              title={t("accounting.transactionsUnavailableTitle")}
              description={t("accounting.transactionsUnavailableDescription")}
            />
          )
        ) : (
          <p className="muted">{t("common.readOnly")}</p>
        )}
      </section>
    </div>
  );
}
