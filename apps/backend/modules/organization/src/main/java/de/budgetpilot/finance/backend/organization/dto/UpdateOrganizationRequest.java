package de.budgetpilot.finance.backend.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.NonNull;

/**
 * @author Niklas Petermeier
 * @since 19.06.2026
 */
public record UpdateOrganizationRequest(
        @NonNull @NotBlank @Size(max = 255) String name,
        @NonNull @NotBlank @Size(max = 255) String slug,
        @NonNull @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency
) {
}
