package de.budgetpilot.finance.backend.organization.authorization;

import de.budgetpilot.finance.backend.organization.domain.MembershipRole;
import org.jspecify.annotations.NonNull;

import java.util.Set;

/**
 * Resolves effective permissions for an organization member.
 * The default implementation maps built-in roles.
 * A future implementation can merge custom permission groups per organization.
 *
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
public interface OrganizationPermissionResolver {
    /**
     * Resolves permissions for one membership role.
     *
     * @param role membership role
     * @return effective permissions
     */
    @NonNull Set<OrganizationPermission> resolve(@NonNull MembershipRole role);
}
