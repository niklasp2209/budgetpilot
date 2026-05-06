package de.budgetpilot.finance.backend.accounting.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 06.05.2026
 */
public record CreateTransactionRequest(
        @NonNull @NotNull UUID accountId,
        @NonNull @NotNull UUID categoryId,
        long amountCents,
        @NonNull @Pattern(regexp = "^[A-Z]{3}$") String currency,
        @NonNull @NotNull OffsetDateTime bookedAt,
        @Nullable @Size(max = 1024) String description
) {
}

