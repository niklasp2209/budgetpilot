package de.budgetpilot.finance.backend.organization.authorization;

/**
 * Granular organization permissions.
 * Built-in roles map to subsets of these permissions today.
 * Custom organization permission groups can assign them individually later.
 *
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
public enum OrganizationPermission {
    ORGANIZATION_READ,
    MEMBERS_MANAGE,
    INVITES_MANAGE,
    ACCOUNTING_READ,
    ACCOUNTING_WRITE,
    BUDGET_READ,
    BUDGET_WRITE,
    REPORTING_READ
}
