package de.budgetpilot.finance.backend.organization.exception;

import org.jspecify.annotations.NonNull;

/**
 * @author Niklas Petermeier
 * @since 06.05.2026
 */
public class OrganizationMemberOperationException extends RuntimeException {
    public OrganizationMemberOperationException(@NonNull String message) {
        super(message);
    }
}

