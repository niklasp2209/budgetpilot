package de.budgetpilot.finance.backend.organization.dto;

import de.budgetpilot.finance.backend.organization.authorization.OrganizationPermission;
import org.jspecify.annotations.NonNull;

import java.util.Set;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 18.06.2026
 */
public record PermissionGroupResponse(
        @NonNull UUID id,
        @NonNull String name,
        @NonNull Set<OrganizationPermission> permissions
) {
}
