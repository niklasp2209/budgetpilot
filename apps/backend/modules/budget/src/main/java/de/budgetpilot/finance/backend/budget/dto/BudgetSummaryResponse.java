package de.budgetpilot.finance.backend.budget.dto;

import org.jspecify.annotations.NonNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
public record BudgetSummaryResponse(
        @NonNull UUID budgetId,
        @NonNull LocalDate periodStart,
        long totalBudgetCents,
        long totalExpenseCents
) {
}

