package de.budgetpilot.finance.backend.auth.exception;

import org.jspecify.annotations.NonNull;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(@NonNull String message) {
        super(message);
    }
}
