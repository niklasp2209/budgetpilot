package de.budgetpilot.finance.backend.budget.controller;

import de.budgetpilot.finance.backend.budget.dto.*;
import de.budgetpilot.finance.backend.budget.mapper.BudgetMapper;
import de.budgetpilot.finance.backend.budget.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
@RestController
@RequiredArgsConstructor
public class BudgetController {
    private final BudgetService budgetService;
    private final BudgetMapper budgetMapper;

    @PostMapping("/api/v1/organizations/{organizationId}/budgets")
    @ResponseStatus(HttpStatus.CREATED)
    /**
     * Creates a new budget.
     *
     * @param organizationId organization identifier
     * @param request budget creation payload
     * @param jwt authenticated JWT token
     * @return created budget response
     */
    public @NonNull BudgetResponse createBudget(
            @PathVariable @NonNull UUID organizationId,
            @Valid @RequestBody @NonNull CreateBudgetRequest request,
            @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        return budgetMapper.toBudgetResponse(
                budgetService.createBudget(organizationId, extractEmail(jwt), request)
        );
    }

    @GetMapping("/api/v1/organizations/{organizationId}/budgets")
    /**
     * Lists budgets for an organization.
     *
     * @param organizationId organization identifier
     * @param jwt authenticated JWT token
     * @return budgets
     */
    public @NonNull List<BudgetResponse> listBudgets(
            @PathVariable @NonNull UUID organizationId,
            @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        return budgetService.listBudgets(organizationId, extractEmail(jwt)).stream()
                .map(budgetMapper::toBudgetResponse)
                .toList();
    }

    @PutMapping("/api/v1/organizations/{organizationId}/budgets/{budgetId}/items")
    /**
     * Creates or updates one budget item.
     *
     * @param organizationId organization identifier
     * @param budgetId budget identifier
     * @param request item upsert payload
     * @param jwt authenticated JWT token
     * @return budget item response
     */
    public @NonNull BudgetItemResponse upsertItem(
            @PathVariable @NonNull UUID organizationId,
            @PathVariable @NonNull UUID budgetId,
            @Valid @RequestBody @NonNull UpsertBudgetItemRequest request,
            @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        return budgetMapper.toBudgetItemResponse(
                budgetService.upsertItem(organizationId, budgetId, extractEmail(jwt), request)
        );
    }

    @GetMapping("/api/v1/organizations/{organizationId}/budgets/{budgetId}/items")
    /**
     * Lists budget items for one budget.
     *
     * @param organizationId organization identifier
     * @param budgetId budget identifier
     * @param jwt authenticated JWT token
     * @return budget items
     */
    public @NonNull List<BudgetItemDetailResponse> listItems(
            @PathVariable @NonNull UUID organizationId,
            @PathVariable @NonNull UUID budgetId,
            @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        return budgetService.listItems(organizationId, budgetId, extractEmail(jwt));
    }

    @DeleteMapping("/api/v1/organizations/{organizationId}/budgets/{budgetId}/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    /**
     * Deletes one budget item.
     *
     * @param organizationId organization identifier
     * @param budgetId budget identifier
     * @param itemId budget item identifier
     * @param jwt authenticated JWT token
     */
    public void deleteItem(
            @PathVariable @NonNull UUID organizationId,
            @PathVariable @NonNull UUID budgetId,
            @PathVariable @NonNull UUID itemId,
            @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        budgetService.deleteItem(organizationId, budgetId, itemId, extractEmail(jwt));
    }

    @GetMapping("/api/v1/organizations/{organizationId}/budgets/{budgetId}/summary")
    /**
     * Returns the budget summary for one month.
     *
     * @param organizationId organization identifier
     * @param budgetId budget identifier
     * @param jwt authenticated JWT token
     * @return budget summary
     */
    public @NonNull BudgetSummaryResponse summary(
            @PathVariable @NonNull UUID organizationId,
            @PathVariable @NonNull UUID budgetId,
            @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        String email = extractEmail(jwt);
        long totalBudget = budgetService.totalBudgetCents(organizationId, budgetId, email);
        long totalExpense = budgetService.totalExpenseCents(organizationId, budgetId, email);
        de.budgetpilot.finance.backend.budget.domain.BudgetEntity budget = budgetService.getBudget(organizationId, budgetId, email);
        return new BudgetSummaryResponse(budgetId, budget.getPeriodStart(), totalBudget, totalExpense);
    }

    /**
     * Extracts email from JWT subject.
     *
     * @param jwt authenticated JWT token
     * @return subject email
     */
    private @NonNull String extractEmail(@NonNull Jwt jwt) {
        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new IllegalStateException("JWT subject is missing.");
        }
        return subject;
    }
}

