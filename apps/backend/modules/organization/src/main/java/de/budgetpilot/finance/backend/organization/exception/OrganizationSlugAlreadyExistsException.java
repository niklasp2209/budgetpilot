package de.budgetpilot.finance.backend.organization.exception;

import org.jspecify.annotations.NonNull;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
public class OrganizationSlugAlreadyExistsException extends RuntimeException {
    public OrganizationSlugAlreadyExistsException(@NonNull String message) {
        super(message);
    }
}
