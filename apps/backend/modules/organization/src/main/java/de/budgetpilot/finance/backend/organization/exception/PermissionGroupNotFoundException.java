package de.budgetpilot.finance.backend.organization.exception;

import org.jspecify.annotations.NonNull;

/**
 * @author Niklas Petermeier
 * @since 18.06.2026
 */
public class PermissionGroupNotFoundException extends RuntimeException {
    public PermissionGroupNotFoundException(@NonNull String message) {
        super(message);
    }
}
