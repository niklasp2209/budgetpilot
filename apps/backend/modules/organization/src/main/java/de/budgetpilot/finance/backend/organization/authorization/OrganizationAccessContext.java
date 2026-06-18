package de.budgetpilot.finance.backend.organization.authorization;

import de.budgetpilot.finance.backend.organization.domain.MembershipRole;
import org.jspecify.annotations.NonNull;

import java.util.Set;
import java.util.UUID;

/**
 * Resolved access context for one organization member.
 *
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
public record OrganizationAccessContext(
        @NonNull UUID userId,
        @NonNull UUID organizationId,
        @NonNull MembershipRole role,
        @NonNull Set<OrganizationPermission> permissions
) {
    /**
     * Checks whether the context includes one permission.
     *
     * @param permission required permission
     * @return true when granted
     */
    public boolean hasPermission(@NonNull OrganizationPermission permission) {
        return permissions.contains(permission);
    }
}
