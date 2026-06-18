package de.budgetpilot.finance.backend.reporting.dto;

import org.jspecify.annotations.NonNull;

import java.time.OffsetDateTime;

/**
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
public record CashflowReportResponse(
        @NonNull OffsetDateTime from,
        @NonNull OffsetDateTime to,
        long incomeCents,
        long expenseCents,
        long netCents
) {
}
