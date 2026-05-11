package de.budgetpilot.finance.backend.budget.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.NonNull;

import java.time.LocalDate;

/**
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
public record CreateBudgetRequest(
        @NonNull @NotBlank @Size(max = 255) String name,
        @NonNull @NotNull LocalDate periodStart,
        @NonNull @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency
) {
}

