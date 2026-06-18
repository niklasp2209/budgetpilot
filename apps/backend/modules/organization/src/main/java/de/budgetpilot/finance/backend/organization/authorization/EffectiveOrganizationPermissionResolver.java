package de.budgetpilot.finance.backend.organization.authorization;

import de.budgetpilot.finance.backend.organization.domain.MembershipRole;
import de.budgetpilot.finance.backend.organization.domain.OrganizationMemberPermissionGroupEntity;
import de.budgetpilot.finance.backend.organization.domain.OrganizationPermissionGroupEntity;
import de.budgetpilot.finance.backend.organization.repository.OrganizationMemberPermissionGroupRepository;
import de.budgetpilot.finance.backend.organization.repository.OrganizationPermissionGroupRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Merges built-in role permissions with custom organization permission groups.
 *
 * @author Niklas Petermeier
 * @since 18.06.2026
 */
@Component
@Primary
@RequiredArgsConstructor
public class EffectiveOrganizationPermissionResolver implements OrganizationPermissionResolver {
    private final RoleBasedOrganizationPermissionResolver roleBasedOrganizationPermissionResolver;
    private final OrganizationMemberPermissionGroupRepository organizationMemberPermissionGroupRepository;
    private final OrganizationPermissionGroupRepository organizationPermissionGroupRepository;

    /**
     * Resolves effective permissions from role and optional custom groups.
     *
     * @param organizationId organization identifier
     * @param userId member user identifier
     * @param role membership role
     * @return effective permissions
     */
    @Override
    public @NonNull Set<OrganizationPermission> resolve(
            @NonNull UUID organizationId,
            @NonNull UUID userId,
            @NonNull MembershipRole role
    ) {
        Set<OrganizationPermission> effectivePermissions = EnumSet.copyOf(
                roleBasedOrganizationPermissionResolver.resolveRole(role)
        );

        List<OrganizationMemberPermissionGroupEntity> assignments =
                organizationMemberPermissionGroupRepository.findByIdOrganizationIdAndIdUserId(organizationId, userId);
        for (OrganizationMemberPermissionGroupEntity assignment : assignments) {
            UUID groupId = assignment.getId().getGroupId();
            OrganizationPermissionGroupEntity group = organizationPermissionGroupRepository.findById(groupId)
                    .filter(value -> value.getOrganizationId().equals(organizationId))
                    .orElse(null);
            if (group != null) {
                effectivePermissions.addAll(group.getPermissions());
            }
        }

        return Set.copyOf(effectivePermissions);
    }
}
