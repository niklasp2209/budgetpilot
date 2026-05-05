package de.budgetpilot.finance.backend.auth.controller;

import org.jspecify.annotations.NonNull;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
public record ErrorResponse(
        @NonNull String code,
        @NonNull String message
) {
}
