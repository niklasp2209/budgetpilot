package de.budgetpilot.finance.backend.budget.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.NonNull;

import java.time.LocalDate;

/**
 * @author Niklas Petermeier
 * @since 19.06.2026
 */
public record UpdateBudgetRequest(
        @NonNull @NotBlank @Size(max = 255) String name,
        @NonNull @NotNull LocalDate periodStart
) {
}
