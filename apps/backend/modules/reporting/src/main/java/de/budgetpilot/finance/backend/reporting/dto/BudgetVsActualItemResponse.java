package de.budgetpilot.finance.backend.reporting.dto;

import org.jspecify.annotations.NonNull;

import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
public record BudgetVsActualItemResponse(
        @NonNull UUID categoryId,
        @NonNull String categoryName,
        long budgetCents,
        long actualCents
) {
}
