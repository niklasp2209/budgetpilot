package de.budgetpilot.finance.backend.invite.exception;

import org.jspecify.annotations.NonNull;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
public class InviteAccessDeniedException extends RuntimeException {
    /**
     * Creates a forbidden invite operation exception.
     *
     * @param message exception message
     */
    public InviteAccessDeniedException(@NonNull String message) {
        super(message);
    }
}
