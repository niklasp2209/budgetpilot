package de.budgetpilot.finance.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.NonNull;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
public record RegisterRequest(
        @NonNull @NotBlank @Email String email,
        @NonNull @NotBlank @Size(min = 8, max = 128) String password
) {
}
