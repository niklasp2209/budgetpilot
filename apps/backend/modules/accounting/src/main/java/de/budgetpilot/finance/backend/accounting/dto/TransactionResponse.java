package de.budgetpilot.finance.backend.accounting.dto;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 06.05.2026
 */
public record TransactionResponse(
        @NonNull UUID id,
        @NonNull UUID accountId,
        @NonNull UUID categoryId,
        long amountCents,
        @NonNull String currency,
        @NonNull OffsetDateTime bookedAt,
        @Nullable String description
) {
}

