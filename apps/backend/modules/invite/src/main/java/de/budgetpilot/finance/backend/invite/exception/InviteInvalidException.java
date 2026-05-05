package de.budgetpilot.finance.backend.invite.exception;

import org.jspecify.annotations.NonNull;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
public class InviteInvalidException extends RuntimeException {
    /**
     * Creates an invalid invite exception.
     *
     * @param message exception message
     */
    public InviteInvalidException(@NonNull String message) {
        super(message);
    }
}
