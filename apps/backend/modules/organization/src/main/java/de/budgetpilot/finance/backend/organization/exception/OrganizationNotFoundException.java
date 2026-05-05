package de.budgetpilot.finance.backend.organization.exception;

import org.jspecify.annotations.NonNull;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
public class OrganizationNotFoundException extends RuntimeException {
    public OrganizationNotFoundException(@NonNull String message) {
        super(message);
    }
}
