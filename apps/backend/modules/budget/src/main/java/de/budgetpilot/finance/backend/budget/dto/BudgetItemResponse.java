package de.budgetpilot.finance.backend.budget.dto;

import org.jspecify.annotations.NonNull;

import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
public record BudgetItemResponse(
        @NonNull UUID id,
        @NonNull UUID categoryId,
        long amountCents
) {
}

