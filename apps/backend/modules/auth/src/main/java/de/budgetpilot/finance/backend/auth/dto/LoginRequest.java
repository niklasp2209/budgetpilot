package de.budgetpilot.finance.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.NonNull;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
public record LoginRequest(
        @NonNull @NotBlank @Email String email,
        @NonNull @NotBlank String password
) {
}
