package de.budgetpilot.finance.backend.budget.dto;

import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
public record UpsertBudgetItemRequest(
        @NonNull @NotNull UUID categoryId,
        long amountCents
) {
}

