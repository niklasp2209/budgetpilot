package de.budgetpilot.finance.backend.accounting.dto;

import de.budgetpilot.finance.backend.accounting.domain.CategoryType;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 06.05.2026
 */
public record CategoryResponse(
        @NonNull UUID id,
        @NonNull String name,
        @NonNull CategoryType type
) {
}

