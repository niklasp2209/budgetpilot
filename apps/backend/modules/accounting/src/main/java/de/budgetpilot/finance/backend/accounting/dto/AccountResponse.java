package de.budgetpilot.finance.backend.accounting.dto;

import org.jspecify.annotations.NonNull;

import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 06.05.2026
 */
public record AccountResponse(
        @NonNull UUID id,
        @NonNull String name,
        @NonNull String currency
) {
}

