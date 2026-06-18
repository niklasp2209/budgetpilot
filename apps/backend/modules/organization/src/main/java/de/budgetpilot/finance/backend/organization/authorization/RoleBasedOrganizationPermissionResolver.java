package de.budgetpilot.finance.backend.organization.authorization;

import de.budgetpilot.finance.backend.organization.domain.MembershipRole;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Maps built-in membership roles to permission sets.
 *
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
@Component
public class RoleBasedOrganizationPermissionResolver implements OrganizationPermissionResolver {
    private static final Map<MembershipRole, Set<OrganizationPermission>> ROLE_PERMISSIONS = buildRolePermissions();

    /**
     * Resolves permissions for one membership role.
     *
     * @param role membership role
     * @return effective permissions
     */
    @Override
    public @NonNull Set<OrganizationPermission> resolve(@NonNull MembershipRole role) {
        return ROLE_PERMISSIONS.getOrDefault(role, Set.of());
    }

    private static @NonNull Map<MembershipRole, Set<OrganizationPermission>> buildRolePermissions() {
        Set<OrganizationPermission> viewerPermissions = EnumSet.of(
                OrganizationPermission.ORGANIZATION_READ,
                OrganizationPermission.ACCOUNTING_READ,
                OrganizationPermission.BUDGET_READ,
                OrganizationPermission.REPORTING_READ
        );

        Set<OrganizationPermission> memberPermissions = EnumSet.copyOf(viewerPermissions);
        memberPermissions.add(OrganizationPermission.ACCOUNTING_WRITE);
        memberPermissions.add(OrganizationPermission.BUDGET_WRITE);

        Set<OrganizationPermission> adminPermissions = EnumSet.copyOf(memberPermissions);
        adminPermissions.add(OrganizationPermission.MEMBERS_MANAGE);
        adminPermissions.add(OrganizationPermission.INVITES_MANAGE);

        Set<OrganizationPermission> ownerPermissions = EnumSet.copyOf(adminPermissions);

        Map<MembershipRole, Set<OrganizationPermission>> permissions = new EnumMap<>(MembershipRole.class);
        permissions.put(MembershipRole.VIEWER, Set.copyOf(viewerPermissions));
        permissions.put(MembershipRole.MEMBER, Set.copyOf(memberPermissions));
        permissions.put(MembershipRole.ADMIN, Set.copyOf(adminPermissions));
        permissions.put(MembershipRole.OWNER, Set.copyOf(ownerPermissions));
        return permissions;
    }
}
