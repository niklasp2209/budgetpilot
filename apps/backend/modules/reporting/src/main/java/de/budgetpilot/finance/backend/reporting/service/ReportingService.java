package de.budgetpilot.finance.backend.reporting.service;

import de.budgetpilot.finance.backend.accounting.domain.CategoryEntity;
import de.budgetpilot.finance.backend.accounting.domain.CategoryType;
import de.budgetpilot.finance.backend.accounting.domain.TransactionEntity;
import de.budgetpilot.finance.backend.accounting.repository.CategoryRepository;
import de.budgetpilot.finance.backend.accounting.repository.TransactionRepository;
import de.budgetpilot.finance.backend.auth.domain.AuthUserEntity;
import de.budgetpilot.finance.backend.auth.repository.AuthUserRepository;
import de.budgetpilot.finance.backend.budget.domain.BudgetEntity;
import de.budgetpilot.finance.backend.budget.domain.BudgetItemEntity;
import de.budgetpilot.finance.backend.budget.repository.BudgetItemRepository;
import de.budgetpilot.finance.backend.budget.repository.BudgetRepository;
import de.budgetpilot.finance.backend.organization.repository.OrganizationMembershipRepository;
import de.budgetpilot.finance.backend.reporting.dto.BudgetVsActualItemResponse;
import de.budgetpilot.finance.backend.reporting.dto.BudgetVsActualReportResponse;
import de.budgetpilot.finance.backend.reporting.dto.CashflowReportResponse;
import de.budgetpilot.finance.backend.reporting.dto.CategoryAmountResponse;
import de.budgetpilot.finance.backend.reporting.exception.ReportingAccessDeniedException;
import de.budgetpilot.finance.backend.reporting.exception.ReportingNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
@Service
@RequiredArgsConstructor
public class ReportingService {
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;
    private final BudgetItemRepository budgetItemRepository;
    private final AuthUserRepository authUserRepository;
    private final OrganizationMembershipRepository organizationMembershipRepository;

    /**
     * Returns income, expense, and net totals for a date range.
     *
     * @param organizationId organization identifier
     * @param authenticatedEmail authenticated requester email
     * @param from optional range start (defaults to 30 days ago)
     * @param to optional range end (defaults to tomorrow)
     * @return cashflow report
     */
    @Transactional(readOnly = true)
    public @NonNull CashflowReportResponse cashflow(
            @NonNull UUID organizationId,
            @NonNull String authenticatedEmail,
            @Nullable OffsetDateTime from,
            @Nullable OffsetDateTime to
    ) {
        requireMember(organizationId, authenticatedEmail);
        OffsetDateTime effectiveFrom = resolveFrom(from);
        OffsetDateTime effectiveTo = resolveTo(to);

        List<TransactionEntity> transactions = transactionRepository.findByOrganizationIdAndBookedAtBetween(
                organizationId, effectiveFrom, effectiveTo
        );
        Map<UUID, CategoryType> categoryTypes = loadCategoryTypes(transactions);

        long incomeCents = 0L;
        long expenseCents = 0L;
        for (TransactionEntity transaction : transactions) {
            CategoryType type = categoryTypes.get(transaction.getCategoryId());
            if (type == CategoryType.INCOME) {
                incomeCents += transaction.getAmountCents();
            } else if (type == CategoryType.EXPENSE) {
                expenseCents += transaction.getAmountCents();
            }
        }

        return new CashflowReportResponse(effectiveFrom, effectiveTo, incomeCents, expenseCents, incomeCents - expenseCents);
    }

    /**
     * Returns expense totals grouped by category for a date range.
     *
     * @param organizationId organization identifier
     * @param authenticatedEmail authenticated requester email
     * @param from optional range start (defaults to 30 days ago)
     * @param to optional range end (defaults to tomorrow)
     * @return expense amounts per category
     */
    @Transactional(readOnly = true)
    public @NonNull List<CategoryAmountResponse> byCategory(
            @NonNull UUID organizationId,
            @NonNull String authenticatedEmail,
            @Nullable OffsetDateTime from,
            @Nullable OffsetDateTime to
    ) {
        requireMember(organizationId, authenticatedEmail);
        OffsetDateTime effectiveFrom = resolveFrom(from);
        OffsetDateTime effectiveTo = resolveTo(to);

        List<TransactionEntity> transactions = transactionRepository.findByOrganizationIdAndBookedAtBetween(
                organizationId, effectiveFrom, effectiveTo
        );
        Map<UUID, CategoryType> categoryTypes = loadCategoryTypes(transactions);
        Map<UUID, Long> amountsByCategory = new HashMap<>();

        for (TransactionEntity transaction : transactions) {
            CategoryType type = categoryTypes.get(transaction.getCategoryId());
            if (type != CategoryType.EXPENSE) {
                continue;
            }
            amountsByCategory.merge(transaction.getCategoryId(), transaction.getAmountCents(), Long::sum);
        }

        if (amountsByCategory.isEmpty()) {
            return List.of();
        }

        Map<UUID, CategoryEntity> categories = loadCategories(amountsByCategory.keySet());
        return amountsByCategory.entrySet().stream()
                .map(entry -> {
                    CategoryEntity category = categories.get(entry.getKey());
                    return new CategoryAmountResponse(
                            entry.getKey(),
                            category != null ? category.getName() : "Unknown",
                            CategoryType.EXPENSE,
                            entry.getValue()
                    );
                })
                .sorted(Comparator.comparing(CategoryAmountResponse::categoryName))
                .toList();
    }

    /**
     * Compares budgeted and actual expense amounts per category for one budget month.
     *
     * @param organizationId organization identifier
     * @param budgetId budget identifier
     * @param authenticatedEmail authenticated requester email
     * @return budget versus actual report
     */
    @Transactional(readOnly = true)
    public @NonNull BudgetVsActualReportResponse budgetVsActual(
            @NonNull UUID organizationId,
            @NonNull UUID budgetId,
            @NonNull String authenticatedEmail
    ) {
        requireMember(organizationId, authenticatedEmail);
        BudgetEntity budget = budgetRepository.findById(budgetId)
                .filter(value -> value.getOrganizationId().equals(organizationId))
                .orElseThrow(() -> new ReportingNotFoundException("Budget not found."));

        LocalDate start = budget.getPeriodStart();
        LocalDate end = start.plusMonths(1);
        OffsetDateTime from = start.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime to = end.atStartOfDay().atOffset(ZoneOffset.UTC);

        List<BudgetItemEntity> budgetItems = budgetItemRepository.findByBudgetId(budgetId);
        Map<UUID, Long> budgetByCategory = new HashMap<>();
        for (BudgetItemEntity item : budgetItems) {
            budgetByCategory.put(item.getCategoryId(), item.getAmountCents());
        }

        List<TransactionEntity> transactions = transactionRepository.findByOrganizationIdAndBookedAtBetween(
                organizationId, from, to
        );
        Map<UUID, CategoryType> categoryTypes = loadCategoryTypes(transactions);
        Map<UUID, Long> actualByCategory = new HashMap<>();
        for (TransactionEntity transaction : transactions) {
            CategoryType type = categoryTypes.get(transaction.getCategoryId());
            if (type == CategoryType.EXPENSE) {
                actualByCategory.merge(transaction.getCategoryId(), transaction.getAmountCents(), Long::sum);
            }
        }

        Set<UUID> categoryIds = new HashSet<>();
        categoryIds.addAll(budgetByCategory.keySet());
        categoryIds.addAll(actualByCategory.keySet());
        if (categoryIds.isEmpty()) {
            return new BudgetVsActualReportResponse(budgetId, List.of());
        }

        Map<UUID, CategoryEntity> categories = loadCategories(categoryIds);
        List<BudgetVsActualItemResponse> items = categoryIds.stream()
                .map(categoryId -> {
                    CategoryEntity category = categories.get(categoryId);
                    return new BudgetVsActualItemResponse(
                            categoryId,
                            category != null ? category.getName() : "Unknown",
                            budgetByCategory.getOrDefault(categoryId, 0L),
                            actualByCategory.getOrDefault(categoryId, 0L)
                    );
                })
                .sorted(Comparator.comparing(BudgetVsActualItemResponse::categoryName))
                .toList();

        return new BudgetVsActualReportResponse(budgetId, items);
    }

    private @NonNull Map<UUID, CategoryType> loadCategoryTypes(@NonNull List<TransactionEntity> transactions) {
        if (transactions.isEmpty()) {
            return Map.of();
        }
        Set<UUID> categoryIds = new HashSet<>();
        for (TransactionEntity transaction : transactions) {
            categoryIds.add(transaction.getCategoryId());
        }
        Map<UUID, CategoryType> categoryTypes = new HashMap<>();
        for (CategoryEntity category : categoryRepository.findAllById(categoryIds)) {
            categoryTypes.put(category.getId(), category.getType());
        }
        return categoryTypes;
    }

    private @NonNull Map<UUID, CategoryEntity> loadCategories(@NonNull Set<UUID> categoryIds) {
        Map<UUID, CategoryEntity> categories = new HashMap<>();
        for (CategoryEntity category : categoryRepository.findAllById(categoryIds)) {
            categories.put(category.getId(), category);
        }
        return categories;
    }

    private @NonNull OffsetDateTime resolveFrom(@Nullable OffsetDateTime from) {
        return from != null ? from : OffsetDateTime.now().minusDays(30);
    }

    private @NonNull OffsetDateTime resolveTo(@Nullable OffsetDateTime to) {
        return to != null ? to : OffsetDateTime.now().plusDays(1);
    }

    private @NonNull AuthUserEntity requireMember(@NonNull UUID organizationId, @NonNull String email) {
        AuthUserEntity user = authUserRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ReportingAccessDeniedException("Authenticated user was not found."));
        boolean isMember = organizationMembershipRepository
                .findByIdOrganizationIdAndIdUserId(organizationId, user.getId())
                .isPresent();
        if (!isMember) {
            throw new ReportingAccessDeniedException("Organization access denied.");
        }
        return user;
    }

    private @NonNull String normalizeEmail(@NonNull String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
