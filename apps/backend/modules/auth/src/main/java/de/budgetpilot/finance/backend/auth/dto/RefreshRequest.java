package de.budgetpilot.finance.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.NonNull;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
public record RefreshRequest(
        @NonNull @NotBlank String refreshToken
) {
}
