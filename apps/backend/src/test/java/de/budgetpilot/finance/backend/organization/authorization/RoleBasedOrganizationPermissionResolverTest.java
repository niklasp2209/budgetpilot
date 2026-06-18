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
        assertTrue(resolver.resolve(MembershipRole.VIEWER).contains(OrganizationPermission.REPORTING_READ));
        assertFalse(resolver.resolve(MembershipRole.VIEWER).contains(OrganizationPermission.ACCOUNTING_WRITE));
        assertFalse(resolver.resolve(MembershipRole.VIEWER).contains(OrganizationPermission.INVITES_MANAGE));
    }

    @Test
    void memberCanWriteFinancialDataButNotManageMembers() {
        assertTrue(resolver.resolve(MembershipRole.MEMBER).contains(OrganizationPermission.ACCOUNTING_WRITE));
        assertTrue(resolver.resolve(MembershipRole.MEMBER).contains(OrganizationPermission.BUDGET_WRITE));
        assertFalse(resolver.resolve(MembershipRole.MEMBER).contains(OrganizationPermission.MEMBERS_MANAGE));
    }

    @Test
    void adminCanManageMembersAndInvites() {
        assertTrue(resolver.resolve(MembershipRole.ADMIN).contains(OrganizationPermission.MEMBERS_MANAGE));
        assertTrue(resolver.resolve(MembershipRole.ADMIN).contains(OrganizationPermission.INVITES_MANAGE));
    }
}
