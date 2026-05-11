package de.budgetpilot.finance.backend.budget.dto;

import org.jspecify.annotations.NonNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
public record BudgetResponse(
        @NonNull UUID id,
        @NonNull String name,
        @NonNull LocalDate periodStart,
        @NonNull String currency
) {
}

