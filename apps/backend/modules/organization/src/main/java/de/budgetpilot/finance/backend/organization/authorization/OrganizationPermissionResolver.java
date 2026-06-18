package de.budgetpilot.finance.backend.organization.authorization;

import de.budgetpilot.finance.backend.organization.domain.MembershipRole;
import org.jspecify.annotations.NonNull;

import java.util.Set;
import java.util.UUID;

/**
 * Resolves effective permissions for an organization member.
 *
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
public interface OrganizationPermissionResolver {
    /**
     * Resolves effective permissions from role and optional custom groups.
     *
     * @param organizationId organization identifier
     * @param userId member user identifier
     * @param role membership role
     * @return effective permissions
     */
    @NonNull Set<OrganizationPermission> resolve(
            @NonNull UUID organizationId,
            @NonNull UUID userId,
            @NonNull MembershipRole role
    );
}
