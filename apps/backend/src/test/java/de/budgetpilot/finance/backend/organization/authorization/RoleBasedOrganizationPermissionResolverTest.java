package de.budgetpilot.finance.backend.organization.authorization;

import de.budgetpilot.finance.backend.organization.domain.MembershipRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
class RoleBasedOrganizationPermissionResolverTest {
    private final RoleBasedOrganizationPermissionResolver resolver = new RoleBasedOrganizationPermissionResolver();

    @Test
    void viewerHasReadPermissionsOnly() {
        assertTrue(resolver.resolveRole(MembershipRole.VIEWER).contains(OrganizationPermission.REPORTING_READ));
        assertFalse(resolver.resolveRole(MembershipRole.VIEWER).contains(OrganizationPermission.ACCOUNTING_WRITE));
        assertFalse(resolver.resolveRole(MembershipRole.VIEWER).contains(OrganizationPermission.INVITES_MANAGE));
    }

    @Test
    void memberCanWriteFinancialDataButNotManageMembers() {
        assertTrue(resolver.resolveRole(MembershipRole.MEMBER).contains(OrganizationPermission.ACCOUNTING_WRITE));
        assertTrue(resolver.resolveRole(MembershipRole.MEMBER).contains(OrganizationPermission.BUDGET_WRITE));
        assertFalse(resolver.resolveRole(MembershipRole.MEMBER).contains(OrganizationPermission.MEMBERS_MANAGE));
        assertFalse(resolver.resolveRole(MembershipRole.MEMBER).contains(OrganizationPermission.PERMISSION_GROUPS_MANAGE));
    }

    @Test
    void adminCanManageMembersInvitesAndPermissionGroups() {
        assertTrue(resolver.resolveRole(MembershipRole.ADMIN).contains(OrganizationPermission.MEMBERS_MANAGE));
        assertTrue(resolver.resolveRole(MembershipRole.ADMIN).contains(OrganizationPermission.INVITES_MANAGE));
        assertTrue(resolver.resolveRole(MembershipRole.ADMIN).contains(OrganizationPermission.PERMISSION_GROUPS_MANAGE));
    }
}
