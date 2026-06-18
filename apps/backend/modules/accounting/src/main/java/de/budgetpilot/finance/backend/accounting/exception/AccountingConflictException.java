package de.budgetpilot.finance.backend.accounting.exception;

import org.jspecify.annotations.NonNull;

/**
 * @author Niklas Petermeier
 * @since 18.06.2026
 */
public class AccountingConflictException extends RuntimeException {
    public AccountingConflictException(@NonNull String message) {
        super(message);
    }
}
