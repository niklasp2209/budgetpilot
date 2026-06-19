package de.budgetpilot.finance.backend.me.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.NonNull;

/**
 * @author Niklas Petermeier
 * @since 19.06.2026
 */
public record ChangePasswordRequest(
        @NonNull @NotBlank String currentPassword,
        @NonNull @NotBlank @Size(min = 8) String newPassword
) {
}
