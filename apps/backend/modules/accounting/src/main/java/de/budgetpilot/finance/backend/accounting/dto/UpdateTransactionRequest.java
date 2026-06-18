package de.budgetpilot.finance.backend.accounting.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 18.06.2026
 */
public record UpdateTransactionRequest(
        @NonNull @NotNull UUID accountId,
        @NonNull @NotNull UUID categoryId,
        long amountCents,
        @NonNull @NotNull OffsetDateTime bookedAt,
        @Nullable @Size(max = 1024) String description
) {
}
