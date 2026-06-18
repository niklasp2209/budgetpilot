package de.budgetpilot.finance.backend.reporting.dto;

import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
public record BudgetVsActualReportResponse(
        @NonNull UUID budgetId,
        @NonNull List<BudgetVsActualItemResponse> items
) {
}
