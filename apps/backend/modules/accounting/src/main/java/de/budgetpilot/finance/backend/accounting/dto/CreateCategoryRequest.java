package de.budgetpilot.finance.backend.accounting.dto;

import de.budgetpilot.finance.backend.accounting.domain.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.NonNull;

/**
 * @author Niklas Petermeier
 * @since 06.05.2026
 */
public record CreateCategoryRequest(
        @NonNull @NotBlank @Size(max = 255) String name,
        @NonNull @NotNull CategoryType type
) {
}

