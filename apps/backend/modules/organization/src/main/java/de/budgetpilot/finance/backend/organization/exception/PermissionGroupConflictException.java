package de.budgetpilot.finance.backend.organization.exception;

import org.jspecify.annotations.NonNull;

/**
 * @author Niklas Petermeier
 * @since 18.06.2026
 */
public class PermissionGroupConflictException extends RuntimeException {
    public PermissionGroupConflictException(@NonNull String message) {
        super(message);
    }
}
