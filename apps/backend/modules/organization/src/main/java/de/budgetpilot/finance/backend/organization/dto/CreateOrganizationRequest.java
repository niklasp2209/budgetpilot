package de.budgetpilot.finance.backend.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.NonNull;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
public record CreateOrganizationRequest(
        @NonNull @NotBlank @Size(max = 255) String name,
        @NonNull @NotBlank @Size(max = 255) String slug
) {
}
