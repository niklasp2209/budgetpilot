package de.budgetpilot.finance.backend.accounting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.NonNull;

/**
 * @author Niklas Petermeier
 * @since 06.05.2026
 */
public record CreateAccountRequest(
        @NonNull @NotBlank @Size(max = 255) String name,
        @NonNull @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency
) {
}

