package de.budgetpilot.finance.backend.organization.dto;

import de.budgetpilot.finance.backend.organization.authorization.OrganizationPermission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.NonNull;

import java.util.Set;

/**
 * @author Niklas Petermeier
 * @since 18.06.2026
 */
public record CreatePermissionGroupRequest(
        @NonNull @NotBlank @Size(max = 255) String name,
        @NonNull @NotNull Set<OrganizationPermission> permissions
) {
}
