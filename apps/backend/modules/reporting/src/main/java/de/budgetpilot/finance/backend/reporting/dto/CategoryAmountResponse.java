package de.budgetpilot.finance.backend.reporting.dto;

import de.budgetpilot.finance.backend.accounting.domain.CategoryType;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
public record CategoryAmountResponse(
        @NonNull UUID categoryId,
        @NonNull String categoryName,
        @NonNull CategoryType type,
        long amountCents
) {
}
