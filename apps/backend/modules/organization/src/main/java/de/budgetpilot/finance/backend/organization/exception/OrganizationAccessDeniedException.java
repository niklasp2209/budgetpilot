package de.budgetpilot.finance.backend.organization.exception;

import org.jspecify.annotations.NonNull;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
public class OrganizationAccessDeniedException extends RuntimeException {
    public OrganizationAccessDeniedException(@NonNull String message) {
        super(message);
    }
}
