package de.budgetpilot.finance.backend.budget.service;

import de.budgetpilot.finance.backend.accounting.domain.CategoryEntity;
import de.budgetpilot.finance.backend.accounting.domain.CategoryType;
import de.budgetpilot.finance.backend.accounting.domain.TransactionEntity;
import de.budgetpilot.finance.backend.accounting.repository.CategoryRepository;
import de.budgetpilot.finance.backend.accounting.repository.TransactionRepository;
import de.budgetpilot.finance.backend.budget.domain.BudgetEntity;
import de.budgetpilot.finance.backend.budget.domain.BudgetItemEntity;
import de.budgetpilot.finance.backend.budget.dto.CreateBudgetRequest;
import de.budgetpilot.finance.backend.budget.dto.UpsertBudgetItemRequest;
import de.budgetpilot.finance.backend.budget.exception.BudgetNotFoundException;
import de.budgetpilot.finance.backend.budget.repository.BudgetItemRepository;
import de.budgetpilot.finance.backend.budget.repository.BudgetRepository;
import de.budgetpilot.finance.backend.organization.authorization.OrganizationAuthorizationService;
import de.budgetpilot.finance.backend.organization.authorization.OrganizationPermission;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
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
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final BudgetItemRepository budgetItemRepository;
    private final OrganizationAuthorizationService organizationAuthorizationService;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Creates a new monthly budget.
     *
     * @param organizationId organization identifier
     * @param authenticatedEmail authenticated requester email
     * @param request budget creation payload
     * @return created budget entity
     */
    @Transactional
    public @NonNull BudgetEntity createBudget(
            @NonNull UUID organizationId,
            @NonNull String authenticatedEmail,
            @NonNull CreateBudgetRequest request
    ) {
        organizationAuthorizationService.requirePermission(
                organizationId, authenticatedEmail, OrganizationPermission.BUDGET_WRITE
        );
        LocalDate periodStart = request.periodStart();
        if (periodStart.getDayOfMonth() != 1) {
            throw new IllegalArgumentException("periodStart must be the first day of month.");
        }
        String currency = request.currency().trim().toUpperCase(Locale.ROOT);
        return budgetRepository.save(BudgetEntity.createNew(organizationId, request.name().trim(), periodStart, currency));
    }

    /**
     * Returns all budgets for an organization.
     *
     * @param organizationId organization identifier
     * @param authenticatedEmail authenticated requester email
     * @return budgets
     */
    @Transactional(readOnly = true)
    public @NonNull List<BudgetEntity> listBudgets(@NonNull UUID organizationId, @NonNull String authenticatedEmail) {
        organizationAuthorizationService.requirePermission(
                organizationId, authenticatedEmail, OrganizationPermission.BUDGET_READ
        );
        return budgetRepository.findByOrganizationId(organizationId);
    }

    /**
     * Creates or updates one budget item for a category.
     *
     * @param organizationId organization identifier
     * @param budgetId budget identifier
     * @param authenticatedEmail authenticated requester email
     * @param request item upsert payload
     * @return budget item entity
     */
    @Transactional
    public @NonNull BudgetItemEntity upsertItem(
            @NonNull UUID organizationId,
            @NonNull UUID budgetId,
            @NonNull String authenticatedEmail,
            @NonNull UpsertBudgetItemRequest request
    ) {
        organizationAuthorizationService.requirePermission(
                organizationId, authenticatedEmail, OrganizationPermission.BUDGET_WRITE
        );
        BudgetEntity budget = budgetRepository.findById(budgetId)
                .filter(value -> value.getOrganizationId().equals(organizationId))
                .orElseThrow(() -> new BudgetNotFoundException("Budget not found."));

        CategoryEntity category = categoryRepository.findById(request.categoryId())
                .filter(value -> value.getOrganizationId().equals(organizationId))
                .orElseThrow(() -> new BudgetNotFoundException("Category not found."));

        if (category.getType() == CategoryType.TRANSFER) {
            throw new IllegalArgumentException("Budget items cannot target TRANSFER categories.");
        }

        Optional<BudgetItemEntity> existing = budgetItemRepository.findByBudgetIdAndCategoryId(budgetId, request.categoryId());
        if (existing.isPresent()) {
            BudgetItemEntity entity = existing.get();
            entity.setAmountCents(request.amountCents());
            return budgetItemRepository.save(entity);
        }

        return budgetItemRepository.save(BudgetItemEntity.createNew(budgetId, category.getId(), request.amountCents()));
    }

    /**
     * Returns the sum of all budget item amounts.
     *
     * @param organizationId organization identifier
     * @param budgetId budget identifier
     * @param authenticatedEmail authenticated requester email
     * @return total budget amount in cents
     */
    @Transactional(readOnly = true)
    public long totalBudgetCents(@NonNull UUID organizationId, @NonNull UUID budgetId, @NonNull String authenticatedEmail) {
        organizationAuthorizationService.requirePermission(
                organizationId, authenticatedEmail, OrganizationPermission.BUDGET_READ
        );
        BudgetEntity budget = budgetRepository.findById(budgetId)
                .filter(value -> value.getOrganizationId().equals(organizationId))
                .orElseThrow(() -> new BudgetNotFoundException("Budget not found."));

        List<BudgetItemEntity> items = budgetItemRepository.findByBudgetId(budget.getId());
        long sum = 0L;
        for (BudgetItemEntity item : items) {
            sum += item.getAmountCents();
        }
        return sum;
    }

    /**
     * Returns the sum of EXPENSE transactions in the budget month.
     *
     * @param organizationId organization identifier
     * @param budgetId budget identifier
     * @param authenticatedEmail authenticated requester email
     * @return total expense amount in cents
     */
    @Transactional(readOnly = true)
    public long totalExpenseCents(@NonNull UUID organizationId, @NonNull UUID budgetId, @NonNull String authenticatedEmail) {
        organizationAuthorizationService.requirePermission(
                organizationId, authenticatedEmail, OrganizationPermission.BUDGET_READ
        );
        BudgetEntity budget = getBudgetOrThrow(organizationId, budgetId);

        LocalDate start = budget.getPeriodStart();
        LocalDate end = start.plusMonths(1);
        OffsetDateTime from = start.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime to = end.atStartOfDay().atOffset(ZoneOffset.UTC);

        List<TransactionEntity> transactions = transactionRepository.findByOrganizationIdAndBookedAtBetween(organizationId, from, to);
        if (transactions.isEmpty()) {
            return 0L;
        }

        Set<UUID> categoryIds = new HashSet<>();
        for (TransactionEntity transaction : transactions) {
            categoryIds.add(transaction.getCategoryId());
        }

        Map<UUID, CategoryType> categoryTypes = new HashMap<>();
        for (CategoryEntity category : categoryRepository.findAllById(categoryIds)) {
            categoryTypes.put(category.getId(), category.getType());
        }

        long sum = 0L;
        for (TransactionEntity transaction : transactions) {
            CategoryType type = categoryTypes.get(transaction.getCategoryId());
            if (type == CategoryType.EXPENSE) {
                sum += transaction.getAmountCents();
            }
        }
        return sum;
    }

    /**
     * Returns one budget if visible to the authenticated member.
     *
     * @param organizationId organization identifier
     * @param budgetId budget identifier
     * @param authenticatedEmail authenticated requester email
     * @return budget entity
     */
    @Transactional(readOnly = true)
    public @NonNull BudgetEntity getBudget(
            @NonNull UUID organizationId,
            @NonNull UUID budgetId,
            @NonNull String authenticatedEmail
    ) {
        organizationAuthorizationService.requirePermission(
                organizationId, authenticatedEmail, OrganizationPermission.BUDGET_READ
        );
        return getBudgetOrThrow(organizationId, budgetId);
    }

    private @NonNull BudgetEntity getBudgetOrThrow(@NonNull UUID organizationId, @NonNull UUID budgetId) {
        return budgetRepository.findById(budgetId)
                .filter(value -> value.getOrganizationId().equals(organizationId))
                .orElseThrow(() -> new BudgetNotFoundException("Budget not found."));
    }
}
