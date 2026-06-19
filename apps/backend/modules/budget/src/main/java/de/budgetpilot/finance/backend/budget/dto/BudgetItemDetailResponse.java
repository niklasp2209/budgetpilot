package de.budgetpilot.finance.backend.budget.dto;

import org.jspecify.annotations.NonNull;

import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 19.06.2026
 */
public record BudgetItemDetailResponse(
        @NonNull UUID id,
        @NonNull UUID categoryId,
        @NonNull String categoryName,
        long amountCents
) {
}
